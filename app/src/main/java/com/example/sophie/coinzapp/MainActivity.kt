package com.example.sophie.coinzapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity

import android.util.Log
import android.view.Menu

import android.view.View

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Geometry
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener{

    private var mapView: MapView? = null
    private var map : MapboxMap? = null
    private val tag = "MainActivity"
    private lateinit var auth:  FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userDB : DocumentReference
    private var shilRate : Double = 0.0 //TODO change this to float? -> double not enough
    private var dolrRate : Double = 0.0
    private var quidRate : Double = 0.0
    private var penyRate : Double = 0.0
    private var iconStyle : String = ""

    private var downloadDate = "" // is in YYYY/MM/DD format
    private val todayDate = SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().time) // TODO change formatting of this (android studio doesnt like)
    private var preferencesFile = "MyPrefsFile"


    private lateinit var originLocation: Location // where the current location is stored at all times
    private lateinit var permissionsManager : PermissionsManager // removes code required for permissions
    private lateinit var locationEngine : LocationEngine // component that gives user location
    private lateinit var locationLayerPlugin : LocationLayerPlugin // provides location awareness to mobile - icons representation of user location

    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        //Mapbox access token
        Mapbox.getInstance(this, "pk.eyJ1Ijoic29waGllbGVhdmVyIiwiYSI6ImNqbXRkdXdkdzE5NXozcWw4cHNtN2RxbjAifQ.qdD_mPQW_dthnmphOmiZRw")
        mapView = findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView?.getMapAsync (this)
        //setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userDB = db.collection("users").document(auth.uid!!)

//        toolbar = supportActionBar!!
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            //TODO implement signout function
            auth.signOut()
            skip_button.setOnClickListener {startActivity(Intent( this, LoginActivity::class.java))}
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
//                toolbar.title = "Map"
                //val mapFragment = MapsFragment.newInstance()
                //openFragment(mapFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bank-> {
                val bankFragment = BankFragment.newInstance()
                openFragment(bankFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shop -> {
                val shopFragment = ShopFragment.newInstance()
                openFragment(shopFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                val settingsFragment = SettingsFragment.newInstance()
                openFragment(settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    private fun openFragment(fragment: android.support.v4.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    @SuppressLint("MissingPermission")
    public override fun onStart() {
        super.onStart()

        //restore user preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        downloadDate = settings.getString("lastDownloadDate", "")
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$downloadDate'")
        iconStyle = settings.getString("lastIconStyle", "original")
        Log.d(tag, "[onStart] Recalled lastIconStyle is '$iconStyle'")
        mapView?.onStart()
        //TODO save and restore coin style preferences

    }

    public override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    public override fun onStop() {
        //turn off permissions on stop
        super.onStop()

        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")

        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit() // editor = makes pref changes
        editor.putString("lastDownloadDate", todayDate)
        editor.putString("lastIconStyle", iconStyle)
        editor.apply() // apply edits

        mapView!!.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
        //locationEngine?.deactivate()
    }



    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null")
        } else {
            map = mapboxMap

            map?.uiSettings?.isCompassEnabled = true // set ui options
            map?.uiSettings?.isZoomControlsEnabled = true

            enableLocation()

            //**DOWNLOAD GEOJSON MAP FOR THE DAY
            //**using async task -> asynctask(input to task, progress info whilst task is running, type of the result returned)

            val testGeoJsonUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/$todayDate/coinzmap.geojson"
            Log.d(tag, "URL is $testGeoJsonUrl")
            val geoJsonDataString = DownloadFileTask(caller = DownloadCompleteRunner).execute(testGeoJsonUrl).get() // TODO check on listener functionality

            val geoJsonSource = GeoJsonSource("geojson", geoJsonDataString) // create new GeoJsonSource from string json map data
            mapboxMap.addSource(geoJsonSource) // add source to the map

            //get today's rates
            getTodaysRates(geoJsonDataString)

            //create map on load
            val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)
            addFeaturesAsUncollectedCoins(featureCollection)
            addUncollectedCoinsToMap()


            //when user nears a coin, remove the coin
        }
    }

    private fun getTodaysRates(geoJsonDataString : String){
        if (downloadDate != todayDate) {
            val todaysRates = JSONObject(geoJsonDataString).getJSONObject("rates")
            shilRate = todaysRates.getDouble("SHIL")
            dolrRate = todaysRates.getDouble("DOLR")
            quidRate = todaysRates.getDouble("QUID")
            penyRate = todaysRates.getDouble("PENY")
        }
    }


    private fun addFeaturesAsUncollectedCoins(featureColl : FeatureCollection){
        //iterate over feature collection and add markers
        Log.d(tag, "adding feature collection coins to database")
        val features = featureColl.features()
        if (features != null && downloadDate != todayDate) {
            for (f in features) {
                if (f !=null) {
                    val point: Geometry? = f.geometry()
                    if (point is Point) {

                        //add marker to database
                        val coin = HashMap<String, Any?>() //TODO add as user collection rather than new collection

                        val snippet = f.getStringProperty("currency") + ", value: " + f.getStringProperty("value")

                        coin.put("lat",point.latitude())
                        coin.put("long",point.longitude())
                        coin.put("currency", f.getStringProperty("currency"))
                        coin.put("value", f.getStringProperty("value"))
                        coin.put("symbol", f.getStringProperty("marker-symbol"))
                        coin.put("snippet", snippet)

                        userDB.collection("uncollectedCoins").document(f.getStringProperty("id")) // check firestore tutorial
                                .set(coin)
                                .addOnSuccessListener { "DocumentSnapshot successfully written" }
                                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

                        //delete nullCoin as no longer needed
                        userDB.collection("uncollectedCoins").document("nullCoin").delete()
                    }
                }
            }
        } else {
            Log.d(tag, "ERROR, feature list is null")
        }
    }

    private fun removeAllMarkers() {
        for(marker : Marker in map!!.markers){
            map!!.removeMarker(marker)
        }
    }

    private fun addUncollectedCoinsToMap(){
        //iterate over feature collection and add markers

        val coinCollection  = userDB.collection("uncollectedCoins")
        Log.d(tag, "adding uncollected coins to map")
        coinCollection.get()
        .addOnCompleteListener ( this) {task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        //TODO only add if id is of correct structure and date is todays date
                        val lat = document.get("lat").toString().toDouble()
                        val long = document.get("long").toString().toDouble()
                        val currency = document.get("currency").toString()
                        val symbol = document.get("symbol").toString()
                        val snippet = document.get("snippet").toString()

                        //find which icon to add
                        val icon = findIcon(currency, symbol)

                        //add marker to the map
                        val markerOptions = MarkerOptions()
                                .position(LatLng(lat, long))
                                .icon(icon)
                                .snippet(snippet)

                        map!!.addMarker(markerOptions) // add the marker to the map
                        //markerCollection.add(Marker(markerOptions)) // add marker to markerCollection to keep track off current markers on map
                    }

                } else {
                    Log.d(tag, "Error getting documents: ", task.exception)
                }
            }


    }


    private fun findIcon(currency : String, coinSymbol : String) : Icon {
        var icons = resources.obtainTypedArray(R.array.original_quid_icons)

        when (currency) {
            "DOLR" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_dollar_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_dollar_icons))
            }
            "PENY" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_penny_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_penny_icons))
            }
            "SHIL" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_shilling_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_shilling_icons))
            }
            "QUID" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_quid_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_quid_icons))
            }
        }

        val iconFile = icons.getResourceId(Integer.parseInt(coinSymbol), 0)
        return IconFactory.getInstance(this).fromResource(iconFile)
    }

    private fun collectCoin(coinID : String, currency : String, coinValue : Float, mapboxMap: MapboxMap) { //TODO make private??
        Log.d(tag, "collectCoin(coinID) begins")

        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result!!

                val goldBeforeCoin = (document.get("goldInBank").toString().toFloat())
                val size = Integer.parseInt(document.get("dailyCoinsCollected").toString())

                Log.d(tag, "gold in bank before coin added = " + goldBeforeCoin.toString())
                Log.d(tag, "current number of collected coins = " + Integer.toString(size))

                if (size < 25) { // if there are less than 25 coins collected
                    //1. CONVERT_COIN_TO_GOLD
                    val rate = getRate(currency).toFloat()
                    val coinValueInGold = rate * coinValue

                    //2. ADD_GOLD_TO_BANK
                    userDB.update("goldInBank", (goldBeforeCoin + coinValueInGold))
                    Log.d(tag, "gold in bank after coin added = " + (goldBeforeCoin + coinValueInGold).toString())

                } else {
                    //3. else ADD_TO_WALLET
                    val coinDoc = userDB.collection("uncollectedCoins").document(coinID).get()
                    userDB.collection("wallet").document(coinID).set(coinDoc)


                }
                //4. remove coin from user uncollected coins
                userDB.collection("uncollectedCoins").document(coinID).delete()
                Log.d(tag, "updating user coins collected with " + (size +1).toString())
                userDB.update("dailyCoinsCollected", size + 1)
                //5.remove coin from map
                //map!!.removeAnnotations()
                removeAllMarkers() // remove all the current markers from the map
                addUncollectedCoinsToMap()
            }
        }

    }

    private fun getRate(currency: String) : Double {
        var rate = 0.0
        when (currency) {
            "DOLR" -> rate = dolrRate
            "PENY" -> rate = penyRate
            "QUID" -> rate = quidRate
            "SHIL" -> rate = shilRate
        }
            //Log.d(tag, String.format("exchange rate for %s is %d", currency, rate))
        //Log.d(tag, String.format("exchange rate for %s is %d", currency, rate))
        return rate
    }

    fun getCoinStyle() : String {
        return iconStyle
    }

    fun setCoinStyle(s : String){
        iconStyle = s
        Log.d(tag, "icon style is now $iconStyle")
        removeAllMarkers()
        addUncollectedCoinsToMap()
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            //if permissions are granted then
            Log.d(tag, "Permissions are granted")
            initialiseLocationEngine()
            initialiseLocationLayer()
        } else {
            // if permissions not granted then activate listener and request location
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()//returns location engine
        locationEngine.apply {
            interval = 5000 // set interval preference for around 5 seconds
            fastestInterval = 1000 // at most every 1 second
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        val lastLocation = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation // set origin loc as the last loc
            setCameraPosition(lastLocation)
        } else // if last location does not exist
        { locationEngine.addLocationEngineListener(this) }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) { Log.d(tag, "map is null") }
            else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin.apply { setLocationLayerEnabled(true) // enables camera tracking location
                    cameraMode = CameraMode.TRACKING // tracks change in loc.
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) { //TODO create a reset orientation/angle/location button???
        // set camera location
        val latlng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }
//
    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(location) // changed from originLocation

            checkCoinLocations(location)
        }
    }

    private fun checkCoinLocations(location: Location){
        var closeToCoin = false
        var coinID = ""

        userDB.collection("uncollectedCoins").get()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            //TODO only add if id is of correct structure and date is todays date
                            val lat = document.get("lat").toString().toDouble()
                            val long = document.get("long").toString().toDouble()
                            val coinLocation = Location("")
                            coinLocation.latitude = lat
                            coinLocation.longitude = long
                            if (location.distanceTo(coinLocation) <= 25) {
                                closeToCoin = true
                                coinID = document.id
                            }

                            if (closeToCoin) {
                                val currency = document.get("currency").toString()
                                val value = (document.get("value").toString()).toFloat()
                                Log.d(tag, "user close to coin $coinID")
                                collectCoin(coinID, currency, value, map!!)
                                closeToCoin = false
                            }
                        }
                    }}


    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        // TODO present a dialog on why access is needed
        Log.d(tag, "Permissions: $permissionsToExplain")
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
        } else {
            // TODO Open a dialogue with the user
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
