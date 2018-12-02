package com.example.sophie.coinzapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
private const val ARG_SHIL = "shilRate"
private const val ARG_DOLR = "dolrRate"
private const val ARG_QUID = "quidRate"
private const val ARG_PENY = "penyRate"

class MapsFragment : Fragment(){
    private var listener: OnFragmentInteractionListener? = null
    private var shilRate : Double = 0.0 //TODO change this to float? -> double not enough
    private var dolrRate : Double = 0.0
    private var quidRate : Double = 0.0
    private var penyRate : Double = 0.0
    private val fragTag = "MapsFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(tag, "hosted by activity " + context.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shilRate = it.getDouble(ARG_SHIL)
            dolrRate = it.getDouble(ARG_DOLR)
            quidRate = it.getDouble(ARG_QUID)
            penyRate = it.getDouble(ARG_PENY)
        }
        Log.d(fragTag, "initialised with rates: Shil $shilRate Dol $dolrRate Quid $quidRate Peny $penyRate")
//
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onStart(){
        super.onStart()
        val shilText : TextView = view!!.findViewById(R.id.text_shilRate)
        shilText.text = shilRate.toString()
        val dolrText : TextView = view!!.findViewById(R.id.text_dolrRate)
        dolrText.text = dolrRate.toString()
        val quidText : TextView = view!!.findViewById(R.id.text_quidRate)
        quidText.text = quidRate.toString()
        val penyText : TextView = view!!.findViewById(R.id.text_penyRate)
        penyText.text = penyRate.toString()

    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(shil : Double, dolr: Double, quid : Double, peny : Double) = MapsFragment().apply {
            arguments = Bundle().apply {
                putDouble(ARG_SHIL, shil)
                putDouble(ARG_DOLR, dolr)
                putDouble(ARG_QUID, quid)
                putDouble(ARG_PENY, peny)

            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        //turn off permissions on stop
        super.onStop()

    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
