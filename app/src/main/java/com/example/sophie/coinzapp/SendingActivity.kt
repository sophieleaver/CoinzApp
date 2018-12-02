package com.example.sophie.coinzapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

import kotlinx.android.synthetic.main.activity_sending.*
import kotlinx.android.synthetic.main.content_sending.*
import kotlinx.android.synthetic.main.user_item.view.*
import org.jetbrains.anko.find
    var coinCurrency : String = ""
    var coinValue : String = ""
    var coinID : String = ""

class SendingActivity : AppCompatActivity(), View.OnClickListener {

    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private var db = FirebaseFirestore.getInstance()
    private var tag = "SendingActivity"
    val users = ArrayList<User_Details>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sending)

        coinCurrency = intent.extras.getString("currency")
        coinValue = intent.extras.getString("value")
        coinID = intent.extras.getString("id")
        Log.d(tag, "sending coin of currency $coinCurrency and value $coinValue")

        setSupportActionBar(toolbar)

        //set listeners for buttons
        val button: Button = find(R.id.button_add_friend) // button for adding a new friend
        button.setOnClickListener(this)
        val button2 : Button = find(R.id.button_back_to_main) // button to navigate back to main activity
        button2.setOnClickListener(this)

        val recycler = recyclerView_friends
        val obj_adapter = CustomAdapter(users)
        recycler.setHasFixedSize(true)

        recycler.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recycler.adapter = obj_adapter

        //add list of friends to recycle view
        val friendCollection = userDB.collection("friends")
        Log.d(tag, "friends from database to recycler view")
        friendCollection.get().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    if (document.id != "nullUser") {
                        Log.d(tag, "adding username: ${document.get("username")}")
                        users.add(User_Details(document.get("username").toString()))
                    }
                }
            } else {
                Log.d(tag, "Error getting documents: ", task.exception)
            }
            val obj_adapter = CustomAdapter(users)
            recyclerView_friends.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            recyclerView_friends.adapter = obj_adapter

        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_add_friend -> { // when you click the add friend button, it adds a new friend to the data base
                addNewFriend()
            }
            R.id.button_back_to_main -> {
                Log.d(tag, "back to main button pressed")
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    fun addNewFriend(){

        val enteredUsername = fieldUsername.text.toString()

        //iterate over user database until find a matching username
        db.collection("users").get().addOnCompleteListener{ task ->
            if (task.isSuccessful!!){

                var foundUser = false // boolean : returns true if a user of specified username exists

                for (document in task.result!!){

                    Log.d(tag, "user ${document.get("username")} is being tested")
                    if (document.get("username") == enteredUsername){
                        foundUser = true
                        val usernameBelongsToCurrentUser = document.get("userID") == userDB.id
                            if(!usernameBelongsToCurrentUser) {
                                addUsernameToFriendsDatabase(document, enteredUsername)
                            } else {
                                Toast.makeText(this,"You have entered your own username, please re-enter", Toast.LENGTH_SHORT).show()
                            }

                    }
                }
                if (!foundUser){ // if a user of specified username was not found
                    Toast.makeText(this, "User not found, please re-enter", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addUsernameToFriendsDatabase(document : QueryDocumentSnapshot, enteredUsername : String){
        //if already has user as friend, return notification
        if(users.contains(User_Details(document.get("username").toString()))){
            Toast.makeText(this, "User is already in friend list", Toast.LENGTH_SHORT).show()
        }
        //else add them to friend list
        else {
            Log.d(tag, "user ${document.get("username")} is being added to friend list")
            val newUser = HashMap<String, Any>()
            newUser.put("username", enteredUsername)
            userDB.collection("friends").document(enteredUsername)
                    .set(newUser)

            users.add(User_Details(document.get("username").toString()))
            fieldUsername.text.clear() // clear entered username
        }
    }

}

//--------------------------------- end of SendingActivity.kt -----------
//---------------------------------start of User Details class ----------
data class User_Details(val name:String)

class CustomAdapter(val userList: ArrayList<User_Details>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: User_Details) {
            val auth = FirebaseAuth.getInstance()
            itemView.usernameView.text=user.name
            itemView.button_send_coin_to_user.setOnClickListener{
                //when button clicked, the coin is added to users unaccepted coin database
                val userDB = FirebaseFirestore.getInstance().collection("users")
                userDB.get().addOnCompleteListener{  task ->
                        if (task.isSuccessful){
                            for (document in task.result!!){
                                if (document.get("username") == user.name){
                                    Toast.makeText(itemView.context, "sending coin...", Toast.LENGTH_SHORT).show()
                                    //add the sent coin to the "friends" unaccepted coins database collection

                                    val sentCoin = HashMap<String, Any>()
                                    sentCoin.put("currency", coinCurrency)
                                    sentCoin.put("value", coinValue)

                                    userDB.document(document.id).collection("unacceptedCoins").add(sentCoin)

                                    //remove coin from current users wallet
                                    userDB.document(auth.uid!!).collection("wallet").document(coinID).delete()
                                    checkForAchievements() //TODO increase sent coins by 1 when coin is sent
                                    //return back to main activity
                                    itemView.context.startActivity(Intent(itemView.context, MainActivity::class.java))
                                }
                            }
                        }
                }


            }
        }

        private fun checkForAchievements() {
            val user = FirebaseAuth.getInstance()
            val userDB = FirebaseFirestore.getInstance().collection("users").document(user.uid!!)
            userDB.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //increase the coins sent by 1 and set the database to this value
                    val coinsSent = Integer.parseInt(task.result!!.get("totalCoinsSent").toString()) + 1
                    Log.d("sendingactivity", "coins sent $coinsSent")
                    userDB.update("totalCoinsSent", coinsSent)

                    val newAchievement = coinsSent == 1 || coinsSent == 100 || coinsSent == 500
                    var achievement = ""
                    when (coinsSent){
                        1 -> achievement = "coinsGiven_F"
                        100 -> achievement = "coinsGiven_KS"
                        500 -> achievement = "coinsGiven_MT"
                    }

                    if (newAchievement){
                        userDB.collection("achievements").document(achievement)
                                .update("status", true)
                    }

                    Toast.makeText(itemView.context, "New achievement!", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}