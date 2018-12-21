import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.sophie.coinzapp.*
import com.example.sophie.coinzapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_bank.*
import kotlinx.android.synthetic.main.coin_item.view.*
import kotlinx.android.synthetic.main.fragment_bank.view.*
import kotlinx.android.synthetic.main.gift_item.view.*
import java.util.*
import kotlin.collections.HashMap

class BankFragment : Fragment(){

    private var goldInBank : Int = 0
    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private var fragTag = "BankFragment"

    override fun onStart(){
        super.onStart()
        //set the 'Gold in Bank' view to current goldInBank on Cloud Firestore
        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result!!

                goldInBank = (document.get("goldInBank").toString().toFloat().toInt())
                gold_in_bank_display.text = "$goldInBank" //TODO there are issues with nulls here to be fixed.

                Log.d(fragTag, "gold in bank = $goldInBank")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_bank, container, false)

        populateWalletRecyclerView(view)
        populateGiftRecyclerView(view)

        return view
    }

    companion object {
        fun newInstance(): BankFragment = BankFragment()
    }

    private fun populateWalletRecyclerView(view : View){

        val coins = ArrayList<Coin_Details>()
        val recyclerWallet = view.recyclerView_wallet
        val obj_adapter_wallet = CustomAdapter(coins)

        recyclerWallet.setHasFixedSize(true)
        recyclerWallet.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerWallet.adapter = obj_adapter_wallet


        val walletCollection = userDB.collection("wallet")
        Log.d(fragTag, "adding collected coins from wallet to recycler view")
        walletCollection.get().addOnCompleteListener(this.activity!!) {task ->
            view.recyclerView_wallet.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView_wallet.adapter = obj_adapter_wallet
        }

        //listens for when there is a change in documents (coins added)
        walletCollection.addSnapshotListener { queryDocumentSnapshots, e ->
            if (e != null) {
                Log.d(fragTag, "Error: ${e.message}")
            }
            for (doc in queryDocumentSnapshots!!.documentChanges) {
                if (doc.type == DocumentChange.Type.ADDED && doc.document.id != "nullCoin") {
                    val currency = doc.document.get("currency").toString()
                    val currencyString = (activity as MainActivity).normaliseCurrencyName(doc.document.get("currency").toString())

                    coins.add(Coin_Details(doc.document.id, currency, currencyString, doc.document.get("value").toString(), R.drawable.bit_coin))
                    view.recyclerView_wallet.adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun populateGiftRecyclerView(view: View){
        val gifts = ArrayList<Gift_Details>()
        val recyclerGifts = view.recyclerView_unacceptedCoins
        val obj_adapter_gifts = CustomGiftAdapter(gifts)

        recyclerGifts.setHasFixedSize(true)
        recyclerGifts.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerGifts.adapter = obj_adapter_gifts

        val giftCollection = userDB.collection("unacceptedCoins")

        //adding unaccepted coins from database to recycler view
        giftCollection.get().addOnCompleteListener(this.activity!!) {task ->
            view.recyclerView_unacceptedCoins.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView_unacceptedCoins.adapter = obj_adapter_gifts
        }

        giftCollection.addSnapshotListener { queryDocumentSnapshots, e ->
            if (e != null) {
                Log.d(fragTag, "Error: ${e.message}")
            }
            for (doc in queryDocumentSnapshots!!.documentChanges) {
                val coinID = doc.document.id
                val currency = doc.document.get("currency").toString()
                val value = doc.document.get("value").toString()

                if (doc.type == DocumentChange.Type.ADDED && doc.document.id != "nullCoin") {
                    Log.d(fragTag, "${doc.document.id}")
                    Log.d(fragTag, "${currency}")
                    Log.d(fragTag, "${value}")
                    Log.d(fragTag, "${context}")
                    gifts.add(Gift_Details(coinID, currency, value, context!!))
                    view.recyclerView_unacceptedCoins.adapter.notifyDataSetChanged()
                }

                if (doc.type == DocumentChange.Type.REMOVED){
                    Log.d(fragTag, " coin removed : currency is $currency")

                    gifts.remove(Gift_Details(coinID, currency, value, context!!))
                    view.recyclerView_unacceptedCoins.adapter.notifyDataSetChanged()

                    //if a coin has been removed then refresh the current gold in bank displayed on the screen
                    userDB.get().addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val document = task.result
                            val goldUpdate = document!!.get("goldInBank").toString().toFloat().toInt()
                            val v = view.findViewById<TextView>(R.id.gold_in_bank_display)
                            v.text = "$goldUpdate"
                        }

                    }
                }
            }
        }
    }
}

//---------------------------------------------------------------
/**
 * Class specifying the details required for a 'coin' object:
 * id = coin unique id for storing in database
 * currency = the raw currency symbol for the coin eg. PENY
 * currencyString = the full string of the currency of the coin eg. Penny
 * strVal = the value of the coin as a string
 * image = the ID for the image that the coin should display in the RecyclerView
 *
 */
data class Coin_Details(val id: String, val currency:String, val currencyString: String, val strVal:String, val image:Int)
//id = coin id
//currency = raw currency token eg. PENY

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

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(coin: Coin_Details) {
            //display the coins currency, value, and image in recycler view
            itemView.textView.text=coin.currencyString
            itemView.textView2.text=coin.strVal
            itemView.imageView.setImageResource(coin.image)

            //if 'send' button clicked then pass coin details to SendingActivity so coin can be sent to another user
            itemView.button_send_coin.setOnClickListener{
                val intent = Intent(itemView.context, SendingActivity::class.java)

                intent.putExtra("id", coin.id)
                intent.putExtra("currency", coin.currency)
                intent.putExtra("value", coin.strVal)

                itemView.context.startActivity(intent) // start SendingActivity
            }
        }
    }
}



//----------------------------------------------------------------------------------------------
/**
 * Specifies the details required for a 'gift' object.
 * id = coin unique id for storing in database
 * currency = the raw currency symbol for the coin eg. PENY
 * strVal = the value of the coin as a string
 *
 * Implements the CustomGiftAdapter required to populate the GiftRecyclerView.
 */
data class Gift_Details(val id: String, val currency: String, val strVal:String, val context: Context)


class CustomGiftAdapter(val giftList: ArrayList<Gift_Details>) : RecyclerView.Adapter<CustomGiftAdapter.ViewHolder>() {

    /**
     * Function is returning the view for each item in the list.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomGiftAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.gift_item, parent, false)
        return ViewHolder(v)
    }

    /**
     * Function is binding the data on the list.
     */
    override fun onBindViewHolder(holder: CustomGiftAdapter.ViewHolder, position: Int) {
        holder.bindItems(giftList[position])

    }

    /**
     * Function returns the size of the list
     */
    override fun getItemCount(): Int {
        return giftList.size
    }

    //the class is holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { //TODO refactor this
        private val user = FirebaseAuth.getInstance()
        private val userDB = FirebaseFirestore.getInstance().collection("users").document(user.uid!!)

        fun bindItems(gift: Gift_Details) {

            itemView.button_view_coin.setOnClickListener{

                userDB.get().addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val user = task.result!!
                        val builder = AlertDialog.Builder(itemView.context)
                        val currencyString = (gift.context as MainActivity).normaliseCurrencyName(gift.currency)


                        //create and build an alert dialog to show the gift's details
                        builder.setTitle("A user has sent you a coin!")
                        builder.setMessage("Another user has sent you a $currencyString - How generous <3\nBut how much? Accept the coin to find out! \nClick Accept to add the coin to your bank or click Discard to throw the coin away! \n\nWARNING: If you discard the coin, it is gone forever.")

                        builder.setPositiveButton("ACCEPT") { dialog, which ->
                            val coinsCollected = user.get("dailyCoinsCollected").toString().toInt()
                            userDB.update("dailyCoinsCollected", coinsCollected + 1)

                            if (coinsCollected < 25){    //if user still within daily limit of collecting coins, coin is added to bank
                                addGoldToBank(user, gift)
                            } else {                     // else the coin is sent to the wallet.
                                addCoinToWallet(gift)
                            }
                            removeGiftFromUnacceptedCoins(gift) // coin is removed from the users unacceptedCoins
                        }

                        builder.setNegativeButton("DISCARD") { dialog, which -> // deletes the coin if the user chooses to click discard
                            Toast.makeText(itemView.context, "Discarding coin... what a waste...", Toast.LENGTH_LONG).show()
                            removeGiftFromUnacceptedCoins(gift)
                        }

                        val dialog: AlertDialog = builder.create()
                        dialog.setCanceledOnTouchOutside(false) // prevents user from clicking out of dialog without making a choice.
                        dialog.show()
                    }
                }


            }
        }

        /**
         * Function to add a coin to the users bank.
         * Converts the coin to gold based on the currency exchange rate and adds the gold to user's
         * current goldInBank total on Cloud Firestore.
         */

        private fun addGoldToBank(user : DocumentSnapshot, gift: Gift_Details) {
            val currentGoldInBank = user.get("goldInBank").toString().toFloat()
            val goldValueOfCoin = (gift.context as MainActivity).getCoinExchangeRate(gift.currency) * gift.strVal.toFloat()
            userDB.update("goldInBank", (currentGoldInBank + goldValueOfCoin))

            Toast.makeText(itemView.context, "Adding $goldValueOfCoin to bank account", Toast.LENGTH_LONG).show()

        }

        /**
         * Function to add a coin to the user's wallet.
         * Used if the user is unable to cash the coin into the bank due to exceeding their limits.
         */
        private fun addCoinToWallet(gift: Gift_Details){
            Toast.makeText(itemView.context, "Cannot add any more coins to the bank today! \n Coin has been added to your wallet", Toast.LENGTH_LONG).show()

            val coin = HashMap<String, Any>()
            coin.put("currency", gift.currency)
            coin.put("value", gift.strVal)

            userDB.collection("wallet").document(gift.id).set(coin)
        }

        /**
         * Function to remove a coin from the unacceptedCoins database.
         * Used on acceptance or rejection of a gift.
         */
        private fun removeGiftFromUnacceptedCoins(gift : Gift_Details) {
            userDB.collection("unacceptedCoins").document(gift.id).delete().addOnSuccessListener {
                Log.d("BankFragment", "coin has been successfully removed ${gift.id}, currency: ${gift.currency}, value: ${gift.strVal}")
            }
        }

    }
}
