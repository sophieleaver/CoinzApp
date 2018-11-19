package com.example.sophie.coinzapp

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_bank.*
import org.jetbrains.anko.find

    var currentStyle = ""

class ShopFragment : Fragment(), View.OnClickListener {
    private var listener: OnFragmentInteractionListener? = null

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

        val button : Button = view.find(R.id.marker_style_button_original)
        button.setOnClickListener(this)
        val button2 : Button = view.find(R.id.marker_style_button_pale)
        button2.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        currentStyle = (activity as MainActivity).getCoinStyle()
        when (v?.id) {
            R.id.marker_style_button_original -> {
                if (currentStyle == "original") {
                    Toast.makeText(context, "Markers are already set to original", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(tag, "setting markers to original")
                    (activity as MainActivity).setCoinStyle("original")
                }
            }
            R.id.marker_style_button_pale -> {
                if (currentStyle == "pale") {
                Toast.makeText(context, "Markers are already set to pale", Toast.LENGTH_SHORT).show()
            } else {
                    Toast.makeText(context, "Markers now being set to pale",Toast.LENGTH_SHORT).show()
                    Log.d(tag, "setting marker style to pale")
                (activity as MainActivity).setCoinStyle("pale")
            }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

//    override fun onDestroyView() {
////        super.onDestroyView()
////        mapView!!.onDestroy()
////        //locationEngine?.deactivate()
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
