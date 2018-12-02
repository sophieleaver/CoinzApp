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
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.find

    var currentStyle = ""

class ShopFragment : Fragment(), View.OnClickListener {
    val userDB = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid!!)
    val userPurchases = userDB.collection("purchases").get()

    val coinCost = 5000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {
        @JvmStatic
        fun newInstance() : ShopFragment = ShopFragment()
        val tag: String = ShopFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_shop, container, false)

        val button : Button = view.find(R.id.button_view_originalCoins)
        button.setOnClickListener(this)
        val buttonPale : Button = view.find(R.id.button_view_paleCoins)
        buttonPale.setOnClickListener(this)
        val buttonDark : Button = view.findViewById(R.id.button_view_darkCoins)
        buttonDark.setOnClickListener(this)
        val buttonParty : Button = view.findViewById(R.id.button_view_partyCoins)
        buttonParty.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        currentStyle = (activity as MainActivity).getCoinStyle()
        when (v?.id) {
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
        }
    }

    fun showViewCoinSetDialog(style : String){
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(style + " Coin Set")
        builder.setMessage("Press select to purchase coins or cancel to return.")

        builder.setNeutralButton("CANCEL"){dialog, which ->
            dialog.cancel()
        }
        val purchaseIndex = findIndex(style)
        val userAlreadyPurchased = userPurchases.result!!.documents.get(purchaseIndex).get("purchased").toString().toBoolean()

        if (userAlreadyPurchased) {
            builder.setPositiveButton("SET COIN STYLE"){dialog, which ->
                setCoinStyle(style)
            }
        } else{
            builder.setPositiveButton("PURCHASE ($coinCost Gold)"){ dialog, which ->
                purchaseCoinSet(style)
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }

    fun findIndex(style : String) : Int{
        var index = 0
        when(style){
            "dark" -> index = 0
            "original" -> index = 1
            "pale" -> index = 2
            "party" ->index = 3
        }
        return index
    }

    fun purchaseCoinSet(style : String){
        //check sufficient gold in bank
        userDB.get().addOnCompleteListener{task ->
            if (task.isSuccessful){
                val document = task.result!!
                val goldInBank = document.get("goldInBank").toString().toFloat().toInt()

                if (goldInBank >= coinCost){ // sufficient gold in bank
                    userDB.update("goldInBank", goldInBank - coinCost)
                    userDB.collection("purchases").document(style).update("purchased", true)
                    setCoinStyle(style)
                    //TODO update amount spent in a bank

                } else{
                    Toast.makeText(this.context, "Insufficient gold in bank to make purchase", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    fun setCoinStyle(style : String){
        if (currentStyle == style) {
            Toast.makeText(context, "Coins are already set to $style", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Coins now being set to $style",Toast.LENGTH_SHORT).show()
            Log.d(tag, "setting marker style to $style")
            (activity as MainActivity).setCoinStyle(style)
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

}
