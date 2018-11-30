import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.sophie.coinzapp.LoginActivity
import com.example.sophie.coinzapp.MainActivity
import com.example.sophie.coinzapp.R
import com.example.sophie.coinzapp.SendingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_bank.*
import kotlinx.android.synthetic.main.coin_item.view.*
import kotlinx.android.synthetic.main.fragment_bank.view.*
import org.jetbrains.anko.find

class BankFragment : Fragment(), View.OnClickListener {
    private var goldInBank : Float = 0.00f
    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private var db = FirebaseFirestore.getInstance()

    private var fragTag = "BankFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_bank, container, false)
        val button : Button = view.find(R.id.button_gold_refresh)
        button.setOnClickListener(this)

        val coins = ArrayList<Coin_Details>()
        val recycler = view.recyclerView
        val obj_adapter = CustomAdapter(coins)
        recycler.setHasFixedSize(true)

        recycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recycler.adapter = obj_adapter
        val walletCollection = userDB.collection("wallet")
        Log.d(fragTag, "adding collected coins from wallet to recycler view")
        walletCollection.get().addOnCompleteListener(this.activity!!) {task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    if(document.id != "nullCoin") {
                        Log.d(fragTag, "adding coin currency: ${document.get("currency").toString()} value : ${document.get("value")}")
                        coins.add(Coin_Details(document.get("currency").toString(), document.get("value").toString(), R.drawable.bit_coin))
                    }
                }
            } else {
                Log.d(tag, "Error getting documents: ", task.exception)
            }
            val obj_adapter = CustomAdapter(coins)
            view.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView.adapter = obj_adapter

        }



        userDB.collection("wallet").addSnapshotListener { queryDocumentSnapshots, e ->

            if (e != null) {
                Log.d(fragTag, "Error: ${e.message}")
            }

            for (doc in queryDocumentSnapshots!!.documentChanges) {
                if (doc.type == DocumentChange.Type.ADDED){
                    val currency = doc.document.get("currency")
                    Log.d(fragTag, " coin added : currency is $currency")
                    coins.add(Coin_Details(doc.document.get("currency").toString(), doc.document.get("value").toString(), R.drawable.icon0_ffff00))


                }
//                if (doc.type == DocumentChange.Type.REMOVED){
//                    val currency = doc.document.get("currency")
//                    Log.d(fragTag, " coin removed : currency is $currency")
//                    coins.remove(Coin_Details(doc.document.get("currency").toString(), doc.document.get("value").toString(), R.drawable.icon0_ffff00))
//
//
//                }
            }

        }


        return view
    }

    override fun onClick(v: View?) {
        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result!!
                goldInBank = (document.get("goldInBank").toString().toFloat())
            }
        }
        when (v?.id) {
            R.id.button_gold_refresh -> {
                gold_in_bank_display.text = "$goldInBank"
            }
        }
    }
    companion object {
        fun newInstance(): BankFragment = BankFragment()
    }
}

//---------------------------------------------------------------

data class Coin_Details(val name:String,val des:String,val image:Int)

class CustomAdapter(val coinList: ArrayList<Coin_Details>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.coin_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(coinList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return coinList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(coin: Coin_Details) {
            itemView.textView.text=coin.name
            itemView.textView2.text=coin.des
            itemView.imageView.setImageResource(coin.image)
            itemView.button_send_coin.setOnClickListener{
                val intent = Intent(itemView.context, SendingActivity::class.java)
                //intent.putExtra("")
                itemView.context.startActivity(intent)
            }
        }
    }
}
