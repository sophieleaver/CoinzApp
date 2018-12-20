package com.example.sophie.coinzapp

import android.annotation.SuppressLint
import android.content.Context
//import android.annotation.TargetApi
import android.support.v7.app.AppCompatActivity
//import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_login.*


/**
 * A login screen that offers the user to login via email and password (using alertdialogs)
 * or to register a new account with an email, password, and unique username.
 */

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private lateinit var auth: FirebaseAuth
    private val tag = "LoginActivity"
    private var username = ""
    private var emailForm: EditText? = null
    private var passwordForm: EditText? = null
    private var usernameForm: EditText? = null
    private val db = FirebaseFirestore.getInstance() //get instance of the database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Set up the login form.

        log_in_button_dialog.setOnClickListener(this)
        register_button_dialog.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.log_in_button_dialog -> {
                //on clicking the log in button an alertdialog appears to allow the user to log in
                openLogInDialog()
            }
            R.id.register_button_dialog -> {
                //on clicking the register button an alertdialog appears to allow the user to log in
                openRegisterDialog()
            }
        }
    }

    /**openLogInDialog() - creates alertdialog to allow user to log in using pre-existing account
     *
     */
    @SuppressLint("InflateParams")
    private fun openLogInDialog() {
        //create alertdialog with layout login_dialog.xml resource file
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.login_dialog, null)
        builder.setView(builderView)

        //find the EditText views in login_dialog.xml and save
        emailForm = builderView.findViewById(R.id.email_form)
        passwordForm = builderView.findViewById(R.id.password_form)

        //set a cancel button
        builder.setNeutralButton("cancel") { dialog, _ -> dialog.cancel() }

        //set a login button
        builder.setPositiveButton("Log In") { _ , _ -> }

        //create the dialog and show on interface
        builder.create()
        val customDialog = builder.show()

        /*on clicking the positive Login button, the user is signed in using the information entered
        in the email and password forms */
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            signIn(emailForm!!.text.toString(), passwordForm!!.text.toString())
        }

    }

    /**creates alertdialog to allow user to register a new account
     *
     */
    @SuppressLint("InflateParams")
    private fun openRegisterDialog() {
        //create alertdialog with layout register_dialog.xml resource file
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.register_dialog, null)
        builder.setView(builderView)

        //find the EditText views in register_dialog.xml and save
        emailForm = builderView.findViewById(R.id.email_form)
        passwordForm = builderView.findViewById(R.id.password_form)
        usernameForm = builderView.findViewById(R.id.username_form)

        //create cancel button
        builder.setNeutralButton("cancel") { dialog, _ -> dialog.cancel() }

        //create positive register button
        builder.setPositiveButton("Register") { _, _ -> }

        //create and show alertdialog
        builder.create()
        val customDialog = builder.show()

        //on clicking the positive Register button, the user is registered using the
        //information entered in the email, username and password forms
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            createAccount(emailForm!!.text.toString(), passwordForm!!.text.toString())
            username = usernameForm!!.text.toString()
        }
    }


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun createAccount(email: String, password: String) {
        Log.d(tag, "createAccount:$email")

        if (!validateFormRegister()) { //checks email, username, and password have been entered and are of correct form
            return
        }
        db.collection("users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (checkUsernameIsUnique(task.result!!)) { // ensure username has unique and not already in use by another user
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task2 ->
                                if (task2.isSuccessful) {

                                    // Sign in success, update UI with the signed-in user's information

                                    Log.d(tag, "createUserWithEmail:success")
                                    val user = auth.currentUser
                                    updateUI(user)

                                    //create new database entry for user

                                    val userAccount = HashMap<String, Any?>() // create new hashmap to store variables in database

                                    userAccount.put("userID", auth.uid!!)
                                    userAccount.put("username", username)
                                    userAccount.put("goldInBank", 0) //the total gold stored in the users account
                                    userAccount.put("totalGoldSpent", 0) //the total gold the user has spent in the shop
                                    userAccount.put("dailyCoinsCollected", 0) //the total number of coins the user has collected on that day
                                    userAccount.put("totalCoinsCollected", 0) //the total number of coins the user has collected since account created
                                    userAccount.put("totalCoinsSent", 0) //the total number of coins the user has sent to other users
                                    userAccount.put("lastDownloadDate", "") //the last date the daily map was downloaded

                                    //create document in "users" with the user uid
                                    val userDB = db.collection("users").document(auth.uid!!)
                                    Log.d(tag, "create new bankaccount in database, userID = " + auth.uid!!)

                                    //set the contents of the users entry in the database to the hashmap userAccount
                                    userDB.set(userAccount).addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

                                    //create
                                    initialiseUserDatabaseVariables(userDB)


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(tag, "createUserWithEmail:failure", task.exception)
                                    Toast.makeText(baseContext, "Invalid email or password, please try again or check network connection",
                                            Toast.LENGTH_SHORT).show()
                                    updateUI(null)

                                }
                            }
                }
            }
        }

    }

    private fun signIn(email: String, password: String) {
        Log.d(tag, "signIn:$email")

        if (!validateFormLogin()) {
            return
        }

        // sign user in with email and password provided
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Email or password incorrect, please re-enter",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    private fun validateFormLogin(): Boolean {
        var valid = true

        val email = emailForm!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailForm!!.error = "Required."
            valid = false
        } else {
            emailForm!!.error = null
        }

        val password = passwordForm!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordForm!!.error = "Required."
            valid = false
        } else {
            passwordForm!!.error = null
        }

        return valid
    }

    /**
     * Attempts to register the new account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */

    private fun validateFormRegister(): Boolean {
        var valid = true

        val email = emailForm!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailForm!!.error = "Required."
            valid = false
        } else {
            emailForm!!.error = null
        }

        val password = passwordForm!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordForm!!.error = "Required."
            valid = false
        } else {
            passwordForm!!.error = null
        }

        val username = usernameForm!!.text.toString()
        if (TextUtils.isEmpty(username)) {
            usernameForm!!.error = "Required."
            valid = false
        } else {
            usernameForm!!.error = null
        }

        return valid
    }

    /**
     * Iterates over "users" collection in Cloud Firestore and checks every username against the entered username.
     * Returns false if the username is already taken.
     */

    private fun checkUsernameIsUnique(users: QuerySnapshot): Boolean {
        var valid = true

        for (user in users) { // iterate over all users in database
            if (user.get("username")!! == username) {
                Toast.makeText(this, "$username is already taken, please choose a new username", Toast.LENGTH_LONG).show()
                valid = false
            }
        }

        return valid
    }

    /**
     * When a user is logged in, the MainActivity begins.
     */

    //@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun updateUI(user: FirebaseUser?) {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        if (!isConnected){ // only start MainActivity if there is a network connection
            Toast.makeText(this, "No network connection detected. Please connect to a network to proceed.", (Toast.LENGTH_LONG)).show()
        } else {
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    /**
     * Creates the collections in the newly creates user entry in "users" that are required for the game.
     * Intialises the uncollectedCoins, unacceptedCoins, wallet, friends, purchases, and achievements collections.
     * uncollectedCoins = contains coins that are still to collected from today's map
     * wallet = coins in the users wallet
     * unacceptedCoins = coins that have been sent to the user by other players
     *
     */

    private fun initialiseUserDatabaseVariables(userDB: DocumentReference) {
        val nullCoin = HashMap<String, Any?>() //this coin is so uncollectedCoins and wallet are not empty

        //create a collection of uncollected coins, initialised containing a null coin
        userDB.collection("uncollectedCoins").document("nullCoin")
                .set(nullCoin)
                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

        userDB.collection("wallet").document("nullCoin") //create a wallet, initialised containing a null coin
                .set(nullCoin)
                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

        userDB.collection("unacceptedCoins").document("nullCoin") // create unacceptedCoins, initialised containing a null coin
                .set(nullCoin).addOnFailureListener { e -> Log.w(tag, "Error adding nullCoin to unacceptedCoins", e) }

        val nullUser = HashMap<String, Any>()
        nullUser.put("username", "")

        userDB.collection("friends").document("nullUser")
                .set(nullUser).addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

        setPurchasedCoins(userDB)
        setPurchasedMaps(userDB)
        setAchievements(userDB)
    }

    /**
     * Creates an entry in the users database to record if coin styles have been purchased from shop.
     * each coin style document in collection has attribute 'purchased' to represent if user has purchased style or not.
     * the Original style is initialised as purchased as it is free.
     */
    private fun setPurchasedCoins(userDB: DocumentReference) {
        //create new collection in user db called purchasedCoins
        val purchases = userDB.collection("purchasedCoins")

        val coinThemePurchaseTrue = HashMap<String, Any>() // create new document entry for purchased themes
        coinThemePurchaseTrue.put("purchased", true)
        purchases.document("original").set(coinThemePurchaseTrue)// set original coin theme as purchased as it is free

        val coinThemePurchaseFalse = HashMap<String, Any>()
        coinThemePurchaseFalse.put("purchased", false)
        purchases.document("dark").set(coinThemePurchaseFalse)//set other coin themes as unpurchased
        purchases.document("pale").set(coinThemePurchaseFalse)
        purchases.document("party").set(coinThemePurchaseFalse)

    }

    /** Creates an entry in the users database to record if map styles have been purchased from shop.
     * each map document in collection has attribute 'purchased' to represent if user has purchased style or not.
     * the Original style is initialised as purchased as it is free.
     */
    private fun setPurchasedMaps(userDB: DocumentReference) {
        //create new collection in user db called purchasedMaps
        val purchases = userDB.collection("purchasedMaps")
        val mapThemePurchaseTrue = HashMap<String, Any>()

        mapThemePurchaseTrue.put("purchased", true)
        purchases.document("original").set(mapThemePurchaseTrue) //initialise original map as already purchased

        val mapThemePurchaseFalse = HashMap<String, Any>()
        mapThemePurchaseFalse.put("purchased", false)
        purchases.document("dark").set(mapThemePurchaseFalse) // set other map themes as unpurchased
        purchases.document("pale").set(mapThemePurchaseFalse)

    }

    /**
     * Creates a new collection in the database and fills with documents to represent each achievement.
     * Each document is initialised with an attribute named 'status'. All achievements are initialised with status as false.
     * status = true then user has gained the achievement
     * status = false then the user is still to gain achivement
     */
    private fun setAchievements(userDB: DocumentReference) {
        val achievement = HashMap<String, Any>()
        achievement.put("status", false) // all achievements are initialised with status = false (they are unachieved)

        userDB.collection("achievements").document("coinsCollected_BFC") // baby's first coin
                .set(achievement)
        userDB.collection("achievements").document("coinsCollected_CE") // coin enthusiast
                .set(achievement)
        userDB.collection("achievements").document("coinsCollected_RoAE") //root of all evil
                .set(achievement)
        userDB.collection("achievements").document("coinsGiven_F") //frugal
                .set(achievement)
        userDB.collection("achievements").document("coinsGiven_KS") // kind soul
                .set(achievement)
        userDB.collection("achievements").document("coinsGiven_MT") // mother theresa
                .set(achievement)
        userDB.collection("achievements").document("goldSpent_CC") // cha ching
                .set(achievement)
        userDB.collection("achievements").document("goldSpent_HBS") // hey big spender
                .set(achievement)
        userDB.collection("achievements").document("goldSpent_SS") // splish splash there goes my cash
                .set(achievement)
    }
}
