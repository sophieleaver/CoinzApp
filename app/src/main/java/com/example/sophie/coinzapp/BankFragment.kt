import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.sophie.coinzapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_bank.*
import org.jetbrains.anko.find

class BankFragment : Fragment(), View.OnClickListener {
    private var goldInBank : Float = 0.00f
    private var userDB = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().uid!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //gold_in_bank_display
        //val tv : TextView = activity!!.findViewById(R.id.gold_in_bank_display)


        val view = inflater.inflate(R.layout.fragment_bank, container, false)
        //gold_in_bank_display.text = "$goldInBank"
        val button : Button = view.find(R.id.button_gold_refresh)
        button.setOnClickListener(this)

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