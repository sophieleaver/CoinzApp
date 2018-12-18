package com.example.sophie.coinzapp

import android.annotation.TargetApi
import android.support.v7.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.login_dialog.*


/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private lateinit var auth:  FirebaseAuth
    private val tag = "LoginActivity"
    private var username = ""
    private var emailForm :EditText? = null
    private var passwordForm :EditText?= null
    private var usernameForm :EditText?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = setContentView(R.layout.activity_login)
        // Set up the login form.

        email_sign_in_button.setOnClickListener { signIn(email_form.text.toString(), password_form.text.toString())}
        email_sign_up_button.setOnClickListener { createAccount(email_form.text.toString(), password_form.text.toString())
                                                    username = fieldUsername.text.toString()}

        log_in_button_dialog.setOnClickListener(this)
        register_button_dialog.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.log_in_button_dialog -> {
                openLogInDiaglog()
            }
            R.id.register_button_dialog -> {
                openRegisterDialog()
            }
        }
    }

    fun openLogInDiaglog(){
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.login_dialog, null)
        builder.setView(builderView)
        emailForm = builderView.findViewById<EditText>(R.id.email_form)
        passwordForm = builderView.findViewById<EditText>(R.id.password_form)

        builder.setNeutralButton("cancel") {
            dialog, id -> dialog.cancel() }

        builder.setPositiveButton("Log In"){ dialogInterface, i -> }

        builder.create()
        val customDialog = builder.show()

        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
            signIn(emailForm!!.text.toString(), passwordForm!!.text.toString())
        }

    }

    fun openRegisterDialog(){
        val builder = AlertDialog.Builder(this)
        val builderView = layoutInflater.inflate(R.layout.register_dialog, null)
        builder.setView(builderView)
        emailForm = builderView.findViewById<EditText>(R.id.email_form)
        passwordForm = builderView.findViewById<EditText>(R.id.password_form)
        usernameForm = builderView.findViewById<EditText>(R.id.username_form)

        builder.setNeutralButton("cancel") {
            dialog, id -> dialog.cancel() }

        builder.setPositiveButton("Register"){ dialogInterface, i -> }

        builder.create()
        val customDialog = builder.show()

        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
            createAccount(emailForm!!.text.toString(), passwordForm!!.text.toString())
            username = usernameForm!!.text.toString()
        }
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]


    private fun createAccount(email: String, password: String) {
        Log.d(tag, "createAccount:$email")

        if (!validateForm()) { //TODO what does this do?
            return
        }

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(tag, "createUserWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)

                        //[START] create new database entry for user

                        val userAccount = HashMap<String, Any?>()
                        val db = FirebaseFirestore.getInstance()
                        userAccount.put("userID", auth.uid!!)
                        userAccount.put("username", username)
                        userAccount.put("goldInBank", 0)
                        userAccount.put("totalGoldSpent", 0)
                        userAccount.put("dailyCoinsCollected",0)
                        userAccount.put("totalCoinsCollected",0)
                        userAccount.put("totalCoinsSent",0)
                        userAccount.put("lastDownloadDate", "")


                        val userDB = db.collection("users").document(auth.uid!!)
                        Log.d(tag, "create new bankaccount in database, userID = " + auth.uid!!)

                        //create new user and add to collection users
                        userDB.set(userAccount).addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }
                        initialiseUserDatabaseVariables(userDB)

                        //[END] create new database entry for user

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Invalid email or password, please try again or check network connection",
                                Toast.LENGTH_SHORT).show()


                        updateUI(null)

                    }
                }
        // [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        Log.d(tag, "signIn:$email")

        if (!validateForm()) {
            return
        }

        // [START sign_in_with_email]
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
                        Toast.makeText(baseContext, "Email or password incorrect, please re-enter or check network connection",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    if (!task.isSuccessful) {
                        //status.setText(R.string.auth_failed)
                    }

                }
        // [END sign_in_with_email]
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */


    private fun validateForm(): Boolean {
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
     * Shows the progress UI and hides the login form.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun updateUI(user: FirebaseUser?) {
        //hideProgressDialog()
        if (user != null) {
            startActivity(Intent( this, MainActivity::class.java))
        }
    }
    private fun initialiseUserDatabaseVariables(userDB :DocumentReference){
        val nullCoin = HashMap<String, Any?>() //this coin is so uncollectedCoins and wallet are not empty

        //create a collection of uncollected coins, initialised with a null coin, for the user
        userDB.collection("uncollectedCoins").document("nullCoin")
                .set(nullCoin)
                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

        //create a wallet, initialised with a null coin, for the user
        userDB.collection("wallet").document("nullCoin")
                .set(nullCoin)
                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

        val nullUser = HashMap<String, Any>()
        nullUser.put("username", "")

        userDB.collection("friends").document("nullUser")
                .set(nullUser)
                .addOnFailureListener{e -> Log.w(tag, "Error adding document", e)}

        userDB.collection("unacceptedCoins").document("nullCoin")
                .set(nullCoin)
                .addOnFailureListener{e -> Log.w(tag, "Error adding nullCoin to unacceptedCoins", e)}
        setPurchasedCoins(userDB)
        setPurchasedMaps(userDB)
        setAchievements(userDB)
    }
    private fun setPurchasedCoins(userDB : DocumentReference){
        val coinThemePurchaseFalse = HashMap<String, Any>()
        coinThemePurchaseFalse.put("purchased", false)

        val coinThemePurchaseTrue = HashMap<String, Any>()
        coinThemePurchaseTrue.put("purchased", true)

        val purchases = userDB.collection("purchasedCoins")

        // set original coin theme as purchased as it is free
        purchases.document("original").set(coinThemePurchaseTrue)
        //set other coin themes as unpurchased
        purchases.document("dark").set(coinThemePurchaseFalse)
        purchases.document("pale").set(coinThemePurchaseFalse)
        purchases.document("party").set(coinThemePurchaseFalse)

    }

    private fun setPurchasedMaps(userDB : DocumentReference){
        val mapThemePurchaseFalse = HashMap<String, Any>()
        mapThemePurchaseFalse.put("purchased", false)

        val mapThemePurchaseTrue = HashMap<String, Any>()
        mapThemePurchaseTrue.put("purchased", true)

        val purchases = userDB.collection("purchasedMaps")

        // set original coin theme as purchased as it is free
        purchases.document("original").set(mapThemePurchaseTrue)
        // set other coin themes as unpurchased
        purchases.document("dark").set(mapThemePurchaseFalse)
        purchases.document("pale").set(mapThemePurchaseFalse)

    }

    private fun setAchievements(userDB : DocumentReference){
        val achievement = HashMap<String, Any>()
        achievement.put("status", false)

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
