package com.example.sophie.coinzapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomNavigationView

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
import org.json.JSONObject


import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener{
    //mapbox and location variables
    private var mapView: MapView? = null
    private var map : MapboxMap? = null
    private lateinit var originLocation: Location // where the current location is stored at all times
    private lateinit var permissionsManager : PermissionsManager // removes code required for permissions
    private lateinit var locationEngine : LocationEngine // component that gives user location
    private lateinit var locationLayerPlugin : LocationLayerPlugin // provides location awareness to mobile - icons representation of user location
    //Firebase authentication and database variables
    private lateinit var auth:  FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userDB : DocumentReference
    //Coin Exchange Rates
    private var shilRate : Double = 0.0 //TODO change this to float? -> double not enough
    private var dolrRate : Double = 0.0
    private var quidRate : Double = 0.0
    private var penyRate : Double = 0.0
    //other variables
    private val tag = "MainActivity"
    private var iconStyle : String = ""
    private var lastDownloadDate = "" // is in YYYY/MM/DD format
    private val todayDate = SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().time) // TODO change formatting of this (android studio doesnt like)
    private var preferencesFile = "MyPrefsFile"
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        //Mapbox access token
        Mapbox.getInstance(this, "pk.eyJ1Ijoic29waGllbGVhdmVyIiwiYSI6ImNqbXRkdXdkdzE5NXozcWw4cHNtN2RxbjAifQ.qdD_mPQW_dthnmphOmiZRw")
        mapView = findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView?.getMapAsync (this)

        //initialise firebase authentication and database instance variables
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userDB = db.collection("users").document(auth.uid!!)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
                val mapFragment = MapsFragment.newInstance()
                openFragment(mapFragment)
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

            //create map on load
            //get the date a map was last downloaded from the users account
            userDB.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "accessing last download date")
                    val document = task.result!!
                    lastDownloadDate = document.get("lastDownloadDate").toString()
                    Log.d(tag, "last download date = $lastDownloadDate, today date = $todayDate")

                    if (lastDownloadDate != "" && lastDownloadDate != todayDate){ //if a map HAS been downloaded previously, delete it
                        removeOldCoinsFromDatabase()
                        val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)
                        addFeaturesAsUncollectedCoins(featureCollection)

                    } else if( lastDownloadDate != todayDate){ //

                        val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)
                        addFeaturesAsUncollectedCoins(featureCollection)

                        //set todays coin exchange rates
                        setDailyExchangeRates(geoJsonDataString)
                    } else { //if the map has already been downloaded today, download the coins that have not been collected
                        addUncollectedCoinsToMap()
                    }
                }
            }

        }
    }

    private fun removeOldCoinsFromDatabase(){
        //remove all the uncollected coins from the users database
        userDB.update("dailyCoinsCollected",0) // TODO check this works
        val coinCollection  = userDB.collection("uncollectedCoins")
        coinCollection.get()
                .addOnCompleteListener ( this) {task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            if (document.id != "nullCoin") {
                                coinCollection.document(document.id).delete()
                            }
                        }
                    } else {
                        Log.d(tag, "Error getting documents: ", task.exception)
                    }
                }

    }

    private fun addFeaturesAsUncollectedCoins(featureColl : FeatureCollection){
        //iterate over feature collection and add markers
        Log.d(tag, "adding feature collection coins to database")
        val features = featureColl.features()
        if (features != null) {
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

                        //userDB.collection("uncollectedCoins").document("nullCoin").delete() //delete the null coin that the collection is initalised with (if it exists)
                    }
                }
            }
        } else {
            Log.d(tag, "ERROR, feature list is null")
        }
        addUncollectedCoinsToMap()
        userDB.update("lastDownloadDate", todayDate) // set the user's date of last map download to today

    }

    private fun removeAllMarkers() {
        for(marker : Marker in map!!.markers){
            map!!.removeMarker(marker)
        }
    }

    private fun addUncollectedCoinsToMap(){
        //iterate over feature collection and add markers

        val coinCollection  = userDB.collection("uncollectedCoins")

        coinCollection.get()
        .addOnCompleteListener ( this) {task ->
                if (task.isSuccessful) {
                    Log.d(tag, "adding uncollected coins to map")
                    for (document in task.result!!) {
                        if (document.id != "nullCoin") {
                            //TODO only add if id is of correct structure and date is todays date
                            val lat = document.get("lat").toString().toDouble()
                            val long = document.get("long").toString().toDouble()
                            val currency = document.get("currency").toString()
                            val symbol = document.get("symbol").toString()
                            val snippet = document.get("snippet").toString()

                            //find which icon to add
                            val icon = findIcon(currency, symbol)

                            map!!.addMarker(MarkerOptions()
                                    .position(LatLng(lat, long))
                                    .icon(icon)
                                    .snippet(snippet)) // add the marker to the map

                        }}
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
        Log.d(tag, "collectCoin $coinID begins")

        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val document = task.result!!

                val goldBeforeCoin = (document.get("goldInBank").toString().toFloat())
                val size = Integer.parseInt(document.get("dailyCoinsCollected").toString())

                if (size <= 25) { // if there are less than 25 coins collected...
                    //...convert the coin to gold
                    val rate = getCoinExchangeRate(currency).toFloat()
                    val coinValueInGold = rate * coinValue

                    //... and add the total exchanged coin's gold total to the users gold total
                    userDB.update("goldInBank", (goldBeforeCoin + coinValueInGold))

                } else { // above 25 coins, add the coin (unexchanged) to the users wallet
                    Log.d(tag, "add to wallet: coinID - value: $coinValue currency: $currency")
                    val coin = HashMap<String,Any?>()
                    coin.put("currency", currency)
                    coin.put("value", coinValue)

                    //val coinDoc = userDB.collection("uncollectedCoins").document(document.id)
                    userDB.collection("wallet").document(coinID).set(coin).addOnFailureListener { e -> Log.w(tag, "Error adding document", e) } // TODO fix?
                }

                //remove the collected coin from user's database collection "uncollectedCoins"

                userDB.collection("uncollectedCoins").document(coinID).delete()
                Log.d(tag, "updating user coins collected with " + (size +1).toString())
                userDB.update("dailyCoinsCollected", size + 1)

                // remove the collected coin from map

                removeAllMarkers() // remove all the current markers from the map
                addUncollectedCoinsToMap() //re-add all the uncollected coins stored in the database to the map
            }
        }

    }

    private fun setDailyExchangeRates(geoJsonDataString : String) {
        val todaysRates = JSONObject(geoJsonDataString).getJSONObject("rates")

        shilRate = todaysRates.getDouble("SHIL")
        dolrRate = todaysRates.getDouble("DOLR")
        quidRate = todaysRates.getDouble("QUID")
        penyRate = todaysRates.getDouble("PENY")
    }

    private fun getCoinExchangeRate(currency: String) : Double {
        var rate = 0.0
        when (currency) {
            "DOLR" -> rate = dolrRate
            "PENY" -> rate = penyRate
            "QUID" -> rate = quidRate
            "SHIL" -> rate = shilRate
        }
        return rate
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(location) // changed from originLocation
            checkCoinLocations(location)
        }
    }

    private fun checkCoinLocations(location: Location) {
        var closeToCoin = false
        var coinID = ""

        userDB.collection("uncollectedCoins").get()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            //TODO only add if id is of correct structure and date is todays date
                            if (document.id != "nullCoin") {
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
                        }
                    }
                }
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

//------ all Location and permissions methods ------

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
            interval = 1000 // set interval preference for around 1 seconds
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

//-------------- methods for change of app state -------------------

    @SuppressLint("MissingPermission")
    public override fun onStart() {
        super.onStart()

        //restore user preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)

        //access the last chosen coin icon style
        iconStyle = settings.getString("lastIconStyle", "original")
        Log.d(tag, "[onStart] Recalled lastIconStyle is '$iconStyle'")
        mapView?.onStart()


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

        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit() // editor = makes pref changes
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
    }

}
