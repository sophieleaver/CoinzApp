import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.widget.*
import com.example.sophie.coinzapp.*
import com.example.sophie.coinzapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_bank.*
import kotlinx.android.synthetic.main.coin_item.view.*
import kotlinx.android.synthetic.main.fragment_bank.view.*
import kotlinx.android.synthetic.main.gift_item.view.*
import org.jetbrains.anko.find
import java.util.*
import kotlin.collections.HashMap

class BankFragment : Fragment(), View.OnClickListener {

    private var goldInBank : Int = 0
    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)
    private var db = FirebaseFirestore.getInstance()

    private var fragTag = "BankFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_bank, container, false)
        val button : Button = view.find(R.id.button_gold_refresh)
        button.setOnClickListener(this)

        val coins = ArrayList<Coin_Details>()
        val recyclerWallet = view.recyclerView_wallet
        val obj_adapter_wallet = CustomAdapter(coins)

        recyclerWallet.setHasFixedSize(true)
        recyclerWallet.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerWallet.adapter = obj_adapter_wallet

        val walletCollection = userDB.collection("wallet")
        Log.d(fragTag, "adding collected coins from wallet to recycler view")
        walletCollection.get().addOnCompleteListener(this.activity!!) {task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    if(document.id != "nullCoin") {
                        Log.d(fragTag, "adding coin currency: ${document.get("currency").toString()} value : ${document.get("value")}")
                        coins.add(Coin_Details(document.id,document.get("currency").toString(), document.get("value").toString(), R.drawable.bit_coin))
                    }
                }
            } else {
                Log.d(tag, "Error getting documents: ", task.exception)
            }
            val obj_adapter_wallet = CustomAdapter(coins)
            view.recyclerView_wallet.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView_wallet.adapter = obj_adapter_wallet

        }

        val gifts = ArrayList<Gift_Details>()
        val recyclerGifts = view.recyclerView_unacceptedCoins
        val obj_adapter_gifts = CustomGiftAdapter(gifts)

        recyclerGifts.setHasFixedSize(true)
        recyclerGifts.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerGifts.adapter = obj_adapter_gifts

        val giftCollection = userDB.collection("unacceptedCoins")
        Log.d(fragTag, "adding unaccepted coins from database to recycler view")
        giftCollection.get().addOnCompleteListener(this.activity!!) {task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {

                        Log.d(fragTag, "adding coin currency: ${document.get("currency").toString()} value : ${document.get("value")}")
                        gifts.add(Gift_Details(document.id,document.get("currency").toString(), document.get("value").toString()))


                }
            } else {
                Log.d(tag, "Error getting documents: ", task.exception)
            }
            val obj_adapter_gifts = CustomGiftAdapter(gifts)
            view.recyclerView_unacceptedCoins.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
            view.recyclerView_unacceptedCoins.adapter = obj_adapter_gifts

        }





        userDB.collection("wallet").addSnapshotListener { queryDocumentSnapshots, e ->

            if (e != null) {
                Log.d(fragTag, "Error: ${e.message}")
            }

            for (doc in queryDocumentSnapshots!!.documentChanges) {
                if (doc.type == DocumentChange.Type.ADDED){
                    val currency = doc.document.get("currency")
                    Log.d(fragTag, " coin added : currency is $currency")
                    coins.add(Coin_Details(doc.document.id,doc.document.get("currency").toString(), doc.document.get("value").toString(), R.drawable.icon0_ffff00))


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

    override fun onStart(){
        super.onStart()
        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result!!
                goldInBank = (document.get("goldInBank").toString().toFloat().toInt())
                gold_in_bank_display.text = "$goldInBank"
                Log.d(fragTag, "gold in bank = $goldInBank")
            }
        }

    }

    override fun onClick(v: View?) {

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

data class Coin_Details(val id: String, val name:String, val strVal:String, val image:Int)

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
            itemView.textView2.text=coin.strVal
            itemView.imageView.setImageResource(coin.image)
            itemView.button_send_coin.setOnClickListener{
                val intent = Intent(itemView.context, SendingActivity::class.java)
                intent.putExtra("id", coin.id)
                intent.putExtra("currency", coin.name)
                intent.putExtra("value", coin.strVal)
                itemView.context.startActivity(intent)


            }
        }

    }
}
//-------------------
data class Gift_Details(val id: String, val name: String, val strVal:String)

class CustomGiftAdapter(val giftList: ArrayList<Gift_Details>) : RecyclerView.Adapter<CustomGiftAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomGiftAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.gift_item, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: CustomGiftAdapter.ViewHolder, position: Int) {
        holder.bindItems(giftList[position])

    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return giftList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val user = FirebaseAuth.getInstance()
        val userDB = FirebaseFirestore.getInstance().collection("users").document(user.uid!!)

        fun bindItems(gift: Gift_Details) {

            itemView.button_view_coin.setOnClickListener{

                userDB.get().addOnCompleteListener{ task ->
                    if (task.isSuccessful) {

                        val user = task.result!!

                        val builder = AlertDialog.Builder(itemView.context)
                        builder.setTitle("A user has sent you a coin!")
                        builder.setMessage("Another user has sent you a ${gift.name} - How generous <3\nBut how much? Accept the coin to find out! \nClick Accept to add the coin to your bank or click Discard to throw the coin away! \n\nWARNING: If you discard the coin, it is gone forever.")

                        builder.setPositiveButton("ACCEPT") { dialog, which ->
                            val coinsCollected = user.get("dailyCoinsCollected").toString().toInt()
                            userDB.update("dailyCoinsCollected", coinsCollected + 1)

                            if (coinsCollected < 25){
                                addGoldToBank(user, gift)
                            } else {
                                addCoinToWallet(gift)
                            }
                            removeCoinFromDatabase(gift)
                        }

                        builder.setNegativeButton("DISCARD") { dialog, which ->
                            Toast.makeText(itemView.context, "Discarding coin... what a waste...", Toast.LENGTH_LONG).show()
                            removeCoinFromDatabase(gift)
                        }

                        val dialog: AlertDialog = builder.create()
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.show()
                    }
                }


            }
        }

        private fun addGoldToBank(user : DocumentSnapshot, gift: Gift_Details) {
            val currentGoldInBank = user.get("goldInBank").toString().toFloat()
            val goldValueOfCoin = getCoinExchangeRate(gift.name) * gift.strVal.toFloat()
            userDB.update("goldInBank", (currentGoldInBank + goldValueOfCoin))

            Toast.makeText(itemView.context, "Adding $goldValueOfCoin to bank account", Toast.LENGTH_LONG).show()
        }

        fun addCoinToWallet(gift: Gift_Details){
            Toast.makeText(itemView.context, "Cannot add any more coins to the bank today!", Toast.LENGTH_LONG).show()

            val coin = HashMap<String, Any>()
            coin.put("currency", gift.name)
            coin.put("value", gift.strVal)

            userDB.collection("wallet").document(gift.id).set(coin)
        }

        private fun removeCoinFromDatabase(gift : Gift_Details) {
            userDB.collection("unacceptedCoins").document(gift.id).delete().addOnSuccessListener {
                Log.d("BankFragment", "coin has been successfully removed ${gift.id}, currency: ${gift.name}, value: ${gift.strVal}")
            }
        }

    }
}
