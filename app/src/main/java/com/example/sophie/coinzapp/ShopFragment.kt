package com.example.sophie.coinzapp

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.jetbrains.anko.find

    var currentCoinStyle = ""
    var currentMapStyle = ""

class ShopFragment : Fragment(), View.OnClickListener {
    private val userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private val userPurchasedCoins = userDB.collection("purchasedCoins").get()
    private val userPurchasedMaps = userDB.collection("purchasedMaps").get()
//    var mapbox : MapboxMap? = null
    private val coinCost = 10000
    private val mapCost = 25000
    private val fragTag = "ShopFragment"

    companion object {
        @JvmStatic
        fun newInstance(map : MapboxMap) : ShopFragment = ShopFragment().apply {
//            mapbox = map
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_shop, container, false)

        //set on click listeners for all buttons (to set the coin and map styles)
        val button : Button = view.find(R.id.button_view_originalCoins)
        button.setOnClickListener(this)
        val buttonPale : Button = view.find(R.id.button_view_paleCoins)
        buttonPale.setOnClickListener(this)
        val buttonDark : Button = view.findViewById(R.id.button_view_darkCoins)
        buttonDark.setOnClickListener(this)
        val buttonParty : Button = view.findViewById(R.id.button_view_partyCoins)
        buttonParty.setOnClickListener(this)
        val buttonOriginalTheme : Button = view.find(R.id.button_original_map)
        buttonOriginalTheme.setOnClickListener(this)
        val buttonLightTheme : Button = view.find(R.id.button_pale_map)
        buttonLightTheme.setOnClickListener(this)
        val buttonDarkTheme : Button = view.findViewById(R.id.button_dark_map)
        buttonDarkTheme.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        currentCoinStyle = (activity as MainActivity).getCoinStyle()
        when (v?.id) {

            //when user clicks a button to change the coin style, showViewCoinSetDialog(string) is called which shows an alert dialog
            R.id.button_view_originalCoins -> {
               showViewCoinSetDialog("original")
            }
            R.id.button_view_paleCoins -> {
                showViewCoinSetDialog("pale")
            }
            R.id.button_view_darkCoins -> {
                showViewCoinSetDialog("dark")
            }
            R.id.button_view_partyCoins -> {
                showViewCoinSetDialog("party")
            }

            //same functionality for clicking on a map style
            R.id.button_original_map -> {
                showViewMapDialog("original")
            }
            R.id.button_pale_map -> {
                showViewMapDialog("pale")
            }
            R.id.button_dark_map-> {
                showViewMapDialog("dark")
            }
        }
    }

    fun showViewCoinSetDialog(style : String){
        //create an alert dialog to ask user if they want to purchase/set the coin set
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(style.capitalize() + " Coin Set")
        builder.setMessage("Press select to purchase coins or cancel to return.")

        //set neutral button to cancel action
        builder.setNeutralButton("CANCEL"){dialog, which ->
            dialog.cancel()
        }
        //find whether user has already purchased the coin set (information stored in database)
        val purchaseIndex = findIndex(style)
        val userAlreadyPurchased = userPurchasedCoins.result!!.documents.get(purchaseIndex).get("purchased").toString().toBoolean()

        /*if the user has already purchased the coin set, give them the option
          to set as the coin style*/
        if (userAlreadyPurchased) {
            builder.setPositiveButton("SET COIN STYLE"){dialog, which ->
                setCoinStyle(style)
            }
        }
        // else, give the user the option to purchase the coin set for coinCost amount of gold
        else{
            builder.setPositiveButton("PURCHASE ($coinCost Gold)"){ dialog, which ->
                makePurchase(style, "coin")
            }
        }

        //show the alert dialog to the user
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }

    fun showViewMapDialog(style : String) {
        //similar functionality as showViewCoinSetDialog
        //begin alert dialog
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(style.capitalize() + " Map Set")
        builder.setMessage("Press select to purchase map or cancel to return.")

        //set neutral button to allow cancellation
        builder.setNeutralButton("CANCEL") { dialog, which ->
            dialog.cancel()
        }
        //see if user already purchased the selected map
        val purchaseIndex = findIndex(style)
        val userAlreadyPurchased = userPurchasedMaps.result!!.documents.get(purchaseIndex).get("purchased").toString().toBoolean()
        //if so, display option to set selected map as the current map style
        if (userAlreadyPurchased) {
            builder.setPositiveButton("SET MAP STYLE") { dialog, which ->
                setMapStyle(style)
            }
        }
        // else give user option to purchase selected map for mapCost gold
        else {
            builder.setPositiveButton("PURCHASE ($mapCost Gold)") { dialog, which ->
               // user purchases the style if they do not already own it
                 makePurchase(style, "map")
            }
        }

        //show alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun findIndex(style : String) : Int {
        /*
        find the 'index' of the style in the database:
        The function takes a string 'style' that represents the style of the coin set
        or map.
        Function returns the the index of the style within the users database as the database
        stores by alphabetical order.
        Allows for simplified access of the style in the user database
         */
        var index = 0
        when (style) {
            "dark" -> index = 0
            "original" -> index = 1
            "pale" -> index = 2
            "party" -> index = 3
        }
        return index
    }


    fun makePurchase(style : String, itemType : String) {
        //function to perform purchase of a coin set or a map style

        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result!!
                val goldInBank = document.get("goldInBank").toString().toFloat().toInt()

                //perform this part of the function if the user wishes to purchase a coin set
                if (itemType == "coin"){
                    if (goldInBank >= coinCost) { // check sufficient gold in bank
                        userDB.update("goldInBank", goldInBank - coinCost) // update bank account balance with cost of set reducted
                        userDB.collection("purchasedCoins").document(style).update("purchased", true)
                        setCoinStyle(style) //set the coin style to 'style'
                        updateGoldSpent(document, coinCost)

                    } else {
                        Toast.makeText(this.context, "Insufficient gold in bank to make purchase", Toast.LENGTH_SHORT).show()
                    }
                }

                //else, perform this part of the function is the user wishes to purchase a map style (same functionality as above)
                if (itemType == "map"){
                    if (goldInBank >= mapCost) { // check sufficient gold in bank
                        userDB.update("goldInBank", goldInBank - mapCost)
                        userDB.collection("purchasedMaps").document(style).update("purchased", true)
                        setMapStyle(style)
                        updateGoldSpent(document, mapCost)

                    } else {
                        Toast.makeText(this.context, "Insufficient gold in bank to make purchase", Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
    }

    fun setCoinStyle(style : String){
        //if the current coin style is already this style then do nothing
        if (currentCoinStyle == style) {
            Toast.makeText(context, "Coins are already set to $style", Toast.LENGTH_SHORT).show()
        }
        //else call main activity to perform changing of coin style
        else {
            Toast.makeText(context, "Coins now being set to $style",Toast.LENGTH_SHORT).show()
            Log.d(fragTag, "setting marker style to $style")
            (activity as MainActivity).setCoinStyle(style)
        }
    }

    fun setMapStyle(style : String){
        if (currentMapStyle == style) {
            Toast.makeText(context, "Map is already set to $style", Toast.LENGTH_SHORT).show()
        } else {
//            //use style string to get map identifier
//            val url = getMapStyleURL(style)
//            //use map identifier to get map style URL
//            val urlID = resources.getString(url)

            Toast.makeText(context, "Map now being set to $style",Toast.LENGTH_SHORT).show()
//            Log.d(tag, "setting map style to $style")

            //set as map style
            (activity as MainActivity).setMapStyle(style)
            currentMapStyle = style
        }
    }

    fun updateGoldSpent(document: DocumentSnapshot, cost : Int){
        Log.d(fragTag, "updating the gold spent in shop")
        //update the total gold spent
        var goldSpent = document.get("totalGoldSpent").toString().toInt()
        goldSpent += cost
        userDB.update("totalGoldSpent", goldSpent)
        //check if any new achievements the user could have won

        var achievement = ""
        if (goldSpent >= 10000) {
             if (goldSpent >= 40000) {
                if (goldSpent >= 80000) {
                    achievement ="goldSpent_SS"// splish splash
                } else {
                    achievement ="goldSpent_HBS"// hey big spender
                }
            } else {
                 achievement ="goldSpent_CC"// cha ching
            }
        }
        Log.d(fragTag, "assessing achievement $achievement")

        if (!achievement.equals("")) { //TODO refactor this code
            userDB.collection("achievements").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result!!.documents
                    for (document in documents){
                        if (document.id.equals(achievement)){
                            Log.d(fragTag, "accessing document ${document.id} now")
                            val alreadyAchieved = document.get("status").toString().toBoolean()

                            if (!alreadyAchieved) {
                                userDB.collection("achievements").document(achievement)
                                        .update("status", true).addOnSuccessListener {
                                            Log.d(fragTag, "achievement $achievement successfully updated")
                                        }

                                Toast.makeText(context, "New Achievement!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }


    }

}
