package com.example.sophie.coinzapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap

class MapsFragment : Fragment(){
    private var listener: OnFragmentInteractionListener? = null
    private var mapView: MapView? = null
    private var map : MapboxMap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userDB : DocumentReference
    private var shilRate : Double = 0.0 //TODO change this to float? -> double not enough
    private var dolrRate : Double = 0.0
    private var quidRate : Double = 0.0
    private var penyRate : Double = 0.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(tag, "hosted by activity " + context.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onStart(){
        super.onStart()

    }

//    private fun getTodaysRates(geoJsonDataString : String){
//
//            val todaysRates = JSONObject(geoJsonDataString).getJSONObject("rates")
//            shilRate = todaysRates.getDouble("SHIL")
//            dolrRate = todaysRates.getDouble("DOLR")
//            quidRate = todaysRates.getDouble("QUID")
//            penyRate = todaysRates.getDouble("PENY")
//
//    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MapsFragment()
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
