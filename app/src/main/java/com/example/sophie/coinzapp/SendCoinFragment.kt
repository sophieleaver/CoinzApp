//package com.example.sophie.coinzapp
//
//import android.content.Context
//import android.net.Uri
//import android.os.Bundle
//import android.support.v4.app.Fragment
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.Toast
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.android.synthetic.main.coin_item.view.*
//import kotlinx.android.synthetic.main.content_sending.*
//import kotlinx.android.synthetic.main.user_item.view.*
//import org.jetbrains.anko.find
//
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_NAME = "param1"
//private const val ARG_DES = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Activities that contain this fragment must implement the
// * [SendCoinFragment.OnFragmentInteractionListener] interface
// * to handle interaction events.
// * Use the [SendCoinFragment.newInstance] factory method to
// * create an instance of this fragment.
// *
// */
//class SendCoinFragment : Fragment(), View.OnClickListener {
//
//    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
//    private var db = FirebaseFirestore.getInstance()
//    private var fragTag = "SendCoinFragment"
//    val users = ArrayList<User_Details>()
//
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_NAME)
//            param2 = it.getString(ARG_DES)
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        val view =  inflater.inflate(R.layout.fragment_send_coin, container, false)
//
//
//        val button: Button = view.find(R.id.button_add_friend) // button for adding a new friend
//        button.setOnClickListener(this)
//        val button2 : Button = view.find(R.id.button_back_to_main) // button to navigate back to main activity
//        button2.setOnClickListener(this)
//
//        val recycler = recyclerView_friends
//        val obj_adapter = CustomAdapter(users)
//        recycler.setHasFixedSize(true)
//
//        recycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
//        recycler.adapter = obj_adapter
//
//        //add list of friends to recycle view
//        val friendCollection = userDB.collection("friends")
//        Log.d(tag, "friends from database to recycler view")
//        friendCollection.get().addOnCompleteListener(this.activity!!) { task ->
//            if (task.isSuccessful) {
//                for (document in task.result!!) {
//                    if (document.id != "nullUser") {
//                        Log.d(tag, "adding username: ${document.get("username")}")
//                        users.add(User_Details(document.get("username").toString()))
//                    }
//                }
//            } else {
//                Log.d(tag, "Error getting documents: ", task.exception)
//            }
//            val obj_adapter = CustomAdapter(users)
//            recyclerView_friends.layoutManager = LinearLayoutManager(this.activity!!, LinearLayout.VERTICAL, false)
//            recyclerView_friends.adapter = obj_adapter
//
//        }
//        return view
//
//    }
//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.button_add_friend -> { // when you click the add friend button, it adds a new friend to the data base
//                val enteredUsername = fieldUsername.text.toString()
//                //check new friend exists:
//                //iterate over user database until find a matching username
//                //TODO is there a way to break when you find it? -> maybe just access directly????
//                db.collection("users").get().addOnCompleteListener{ task ->
//                    if (task.isSuccessful!!){
//                        var foundUser = false
//                        for (document in task.result!!){
//                            Log.d(tag, "user ${document.get("username")} is being tested")
//                            if (document.get("username") == enteredUsername){ // cant be own username
//                                foundUser = true
//                                if(document.get("userID") != userDB.id) {
//                                    Log.d(tag, "user ${document.get("username")} is being added to friend list")
//                                    val newUser = HashMap<String, Any>()
//                                    newUser.put("username", enteredUsername)
//                                    userDB.collection("friends").document(enteredUsername)
//                                            .set(newUser)
//
//                                    users.add(User_Details(document.get("username").toString()))
//
//
//                                } else {
//                                    Toast.makeText(context,"You have entered your own username, please re-enter", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        }
//                        if (!foundUser){
//                            Toast.makeText(context, "Username not found, please re-enter", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                }
//                //then it updates the recyclerview
//
//
//            }
//            R.id.button_back_to_main -> {
//                Log.d(tag, "back to main button pressed")
//                val mapFragment =  MapsFragment()
//                this.activity!!.supportFragmentManager.beginTransaction()
//                        .replace(R.id.container, mapFragment, "Find this Fragment")
//                        .addToBackStack(null)
//                        .commit()
////                startActivity(Intent(this, MainActivity::class.java))
//            }
//        }
//    }
//
//
//
//    companion object {
//
//        @JvmStatic
//        fun newInstance(coinName: String, coinDes: String) =
//                SendCoinFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(ARG_NAME, coinName)
//                        putString(ARG_DES, coinDes)
//                    }
//                }
//    }
//}
//
//data class User_Details(val name:String)
//
//class CustomAdapter(val userList: ArrayList<User_Details>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
//
//    //this method is returning the view for each item in the list
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
//        val v = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
//        return ViewHolder(v)
//    }
//
//    //this method is binding the data on the list
//    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
//        holder.bindItems(userList[position])
//    }
//
//    //this method is giving the size of the list
//    override fun getItemCount(): Int {
//        return userList.size
//    }
//
//    //the class is holding the list view
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        fun bindItems(user: User_Details) {
//            itemView.imageView.button_send_coin_to_user.setOnClickListener{
//                //send coin to
//            }
//        }
//    }
//}