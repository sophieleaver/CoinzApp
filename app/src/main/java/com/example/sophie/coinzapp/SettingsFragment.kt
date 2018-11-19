import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.sophie.coinzapp.LoginActivity
import com.example.sophie.coinzapp.R
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.find
import android.app.Activity



class SettingsFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val button : Button = view.find(R.id.button_log_out)
        button.setOnClickListener(this)
        return view
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun onClick(v: View?) {
     when (v?.id){
         R.id.button_log_out -> {
             val auth = FirebaseAuth.getInstance().signOut()
             startActivity(Intent(activity, LoginActivity::class.java))
             (activity as Activity).overridePendingTransition(0, 0)
         }
     }
    }
}