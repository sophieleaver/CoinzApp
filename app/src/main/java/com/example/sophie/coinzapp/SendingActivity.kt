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

import kotlinx.android.synthetic.main.activity_sending.*
import kotlinx.android.synthetic.main.coin_item.view.*
import kotlinx.android.synthetic.main.content_sending.*
import kotlinx.android.synthetic.main.user_item.view.*
import org.jetbrains.anko.find

class SendingActivity : AppCompatActivity(), View.OnClickListener {

    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private var db = FirebaseFirestore.getInstance()
    private var tag = "SendingActivity"
    val users = ArrayList<User_Details>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sending)
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
                val enteredUsername = fieldUsername.text.toString()
                //check new friend exists:
                //iterate over user database until find a matching username
                //TODO is there a way to break when you find it? -> maybe just access directly????
                db.collection("users").get().addOnCompleteListener{ task ->
                    if (task.isSuccessful!!){
                        var foundUser = false
                        for (document in task.result!!){
                            Log.d(tag, "user ${document.get("username")} is being tested")
                            if (document.get("username") == enteredUsername){ // cant be own username
                                foundUser = true
                                if(document.get("userID") != userDB.id) {
                                    Log.d(tag, "user ${document.get("username")} is being added to friend list")
                                    val newUser = HashMap<String, Any>()
                                    newUser.put("username", enteredUsername)
                                    userDB.collection("friends").document(enteredUsername)
                                            .set(newUser)

                                    users.add(User_Details(document.get("username").toString()))


                                } else {
                                    Toast.makeText(this,"You have entered your own username, please re-enter", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        if (!foundUser){
                            Toast.makeText(this, "Username not found, please re-enter", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                //then it updates the recyclerview


            }
            R.id.button_back_to_main -> {
                Log.d(tag, "back to main button pressed")
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}



//        userDB.collection("wallet").addSnapshotListener { queryDocumentSnapshots, e ->
//
//            if (e != null) {
//                Log.d(fragTag, "Error: ${e.message}")
//            }
//
//            for (doc in queryDocumentSnapshots!!.documentChanges) {
//                if (doc.type == DocumentChange.Type.ADDED){
//                    val currency = doc.document.get("currency")
//                    Log.d(fragTag, " coin added : currency is $currency")
//                    coins.add(Coin_Details(doc.document.get("currency").toString(), doc.document.get("value").toString(), R.drawable.icon0_ffff00))
//
//
//                }
//                if (doc.type == DocumentChange.Type.REMOVED){
//                    val currency = doc.document.get("currency")
//                    Log.d(fragTag, " coin removed : currency is $currency")
//                    coins.remove(Coin_Details(doc.document.get("currency").toString(), doc.document.get("value").toString(), R.drawable.icon0_ffff00))
//
//
//                }


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
            itemView.imageView.button_send_coin_to_user.setOnClickListener{
                //send coin to
            }
        }
    }
}