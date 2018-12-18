import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.sophie.coinzapp.LoginActivity
import com.example.sophie.coinzapp.R
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.find
import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.acheivement_item.view.*
import kotlinx.android.synthetic.main.fragment_bank.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.util.ArrayList


class SettingsFragment : Fragment(), View.OnClickListener {
    private val ARG_username = "username"
    private var username = ""
    private val fragTag = "SettingsFragment"
    val user = FirebaseAuth.getInstance()
    val userDB = FirebaseFirestore.getInstance().collection("users").document(user.uid!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments.let {
            username = it!!.getString(ARG_username)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val button : Button = view.find(R.id.button_log_out)
        button.setOnClickListener(this)

        //set up recycler view for acheivements
        createAchievementRecyclerView(view)
        return view
    }

    override fun onStart() {
        super.onStart()
        val usernameText : TextView = view!!.findViewById(R.id.text_username)
        usernameText.text = username
    }
    companion object {
        fun newInstance(username : String): SettingsFragment = SettingsFragment().apply {
            arguments = Bundle().apply{
                putString(ARG_username, username)
            }
        }
    }

    override fun onClick(v: View?) {
     when (v?.id){
         R.id.button_log_out -> {
             val auth = FirebaseAuth.getInstance().signOut()
             startActivity(Intent(activity, LoginActivity::class.java))
             (activity as Activity).overridePendingTransition(0, 0)
         }
     }
    }

    private fun createAchievementRecyclerView(view: View){
        val achievements = ArrayList<Achievement_Details>()

//        val obj_adapter_achievement = AchievementAdapter(achievements)

        view.recyclerView_achievements.setHasFixedSize(true)
//        view.recyclerView_achievements.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
//        view.recyclerView_achievements.adapter = obj_adapter_achievement

        val acheivementsCollection = userDB.collection("achievements")
        Log.d(fragTag, "adding achievements to recycler view")
        acheivementsCollection.get().addOnCompleteListener(this.activity!!) { task ->
            if (task.isSuccessful) {
                val achievementDocs = task.result!!.documents
                achievements.add(Achievement_Details("Baby's first coin", "Picked up 1 coin", achievementDocs.get(0).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Coin Enthusiast", "Picked up 100 coins", achievementDocs.get(1).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("...The root of all evil", "Picked up 1000 coins", achievementDocs.get(2).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Frugal", "Given away 1 coin", achievementDocs.get(3).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Kind Soul", "Given away 100 coins", achievementDocs.get(4).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Mother Theresa", "Given away 500 coins", achievementDocs.get(5).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Cha Ching", "Spent 10,000 gold in the shop", achievementDocs.get(6).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Hey Big Spender", "Spent 40,000 gold in the shop", achievementDocs.get(7).get("status").toString().toBoolean()))
                achievements.add(Achievement_Details("Splish Splash There Goes My Cash", "Spent 80,000 gold in the shop", achievementDocs.get(8).get("status").toString().toBoolean()))

            } else {
                Log.d(tag, "Error getting documents: ", task.exception)
            }
            val obj_adapter_achievements = AchievementAdapter(achievements)
            view.recyclerView_achievements.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView_achievements.adapter = obj_adapter_achievements

        }
    }
}

//----------------------------
data class Achievement_Details(val name: String, val description: String, val status: Boolean)

class AchievementAdapter(val achievementList: ArrayList<Achievement_Details>) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.acheivement_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: AchievementAdapter.ViewHolder, position: Int) {
        holder.bindItems(achievementList[position])

    }

    override fun getItemCount(): Int {
        return achievementList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(achievement: Achievement_Details) {
            itemView.text_achievement_name.text = achievement.name
            itemView.text_achievement_description.text = achievement.description
            if (achievement.status){
                itemView.text_achievement_status.text = "Achieved!"
                itemView.achievement_container.setBackgroundColor(Color.rgb(179,214,126))
            }else{
                itemView.text_achievement_status.text = ""
            }
        }
    }
}
