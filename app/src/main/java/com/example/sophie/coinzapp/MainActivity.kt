package com.example.sophie.coinzapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.design.widget.BottomNavigationView

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity

import android.util.Log

import android.view.View
import android.widget.Toast

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

//Coin Exchange Rates
var shilRate: Double = 0.0
var dolrRate: Double = 0.0
var quidRate: Double = 0.0
var penyRate: Double = 0.0

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {
    //mapbox and location variables
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var originLocation: Location // where the current location is stored at all times
    private lateinit var permissionsManager: PermissionsManager // removes code required for permissions
    private lateinit var locationEngine: LocationEngine // component that gives user location
    private lateinit var locationLayerPlugin: LocationLayerPlugin // provides location awareness to mobile - icons representation of user location

    //Firebase authentication and database variables
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userDB: DocumentReference
    private lateinit var username: String

    //other variables
    private val tag = "MainActivity"
    private var iconStyle: String = ""
    private var lastDownloadDate = "" // is in YYYY/MM/DD format
    @SuppressLint("SimpleDateFormat")
    private val todayDate = SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().time)
    private var preferencesFile = "MyPrefsFile"
    lateinit var toolbar: ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        //Mapbox access token
        Mapbox.getInstance(this, "pk.eyJ1Ijoic29waGllbGVhdmVyIiwiYSI6ImNqbXRkdXdkdzE5NXozcWw4cHNtN2RxbjAifQ.qdD_mPQW_dthnmphOmiZRw")
        mapView = findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        //initialise firebase authentication and database instance variables
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userDB = db.collection("users").document(auth.uid!!)
        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                username = task.result!!.get("username").toString()
                Log.d(tag, "username found $username")
            }

        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    /**
     * Function that allows navigation using the bottom navigation bar.
     * When an icon is selected on the BNB, MainActivity creates the appropriate fragment and opens
     * the fragment using OpenFragment(fragment).
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_map -> {
                val mapFragment = MapsFragment.newInstance(shilRate, dolrRate, quidRate, penyRate)
                openFragment(mapFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bank -> {
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
                val settingsFragment = SettingsFragment.newInstance(username)
                openFragment(settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    /**
     * Opens the specified fragment by changing the container currently displayed on screen with
     * the UI of the new fragment.
     */
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
            val geoJsonDataString = DownloadFileTask(caller = DownloadCompleteRunner).execute(testGeoJsonUrl).get()

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

                    if (lastDownloadDate != "" && lastDownloadDate != todayDate) { //if a map HAS been downloaded previously, delete it and download todays map

                        val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)
                        deleteCoinsInWallet()
                        removeCoinsAndDownloadNewMap(featureCollection) //remove old coins before downloading todays map

                    } else if (lastDownloadDate != todayDate) { //if first time downloading a map (user is new)
                        val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)
                        addFeaturesAsUncollectedCoins(featureCollection)
                    } else { //if the map has already been downloaded today, download the coins that have not been collected
                        addUncollectedCoinsToMap()
                    }
                }
            }
            setDailyExchangeRates(geoJsonDataString)
            //once exchange rates have been set, set them in map fragment
            Log.d(tag, "opening mapfragment now")
            setMapStyle(currentMapStyle)
            openFragment(MapsFragment.newInstance(shilRate, dolrRate, quidRate, penyRate))
        }
    }

    /**
     * Removes current markers from the map, initiates addFeaturesAsUncollectedCoins on complete
     * to download todays map
     */
    private fun removeCoinsAndDownloadNewMap(featureCollection: FeatureCollection) {
        //remove all the uncollected coins from the users database
        userDB.update("dailyCoinsCollected", 0)
        val coinCollection = userDB.collection("uncollectedCoins")
        coinCollection.get()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            if (document.id != "nullCoin") {
                                coinCollection.document(document.id).delete()
                            }
                        }
                        //add the new coins to the database
                        addFeaturesAsUncollectedCoins(featureCollection)
                    } else {
                        Log.d(tag, "Error getting documents: ", task.exception)
                    }
                }

    }

    /**
     * Function deletes every coin in the wallet and the unaccepted coins except the nullCoin
     */
    private fun deleteCoinsInWallet(){
        userDB.collection("wallet").get().addOnCompleteListener {task ->
            if (task.isSuccessful){
                for (coin in task.result!!){
                    if (coin.id != "nullCoin") {
                        userDB.collection("wallet").document(coin.id).delete()
                    }
                }
            }
        }
        userDB.collection("unacceptedCoins").get().addOnCompleteListener {task ->
            if (task.isSuccessful){
                for (coin in task.result!!){
                    if (coin.id != "nullCoin") {
                        userDB.collection("unacceptedCoins").document(coin.id).delete()
                    }
                }
            }
        }
    }

    /**
     * Function that adds all features within the FeatureCollection to the database.
     * Creates a new document for each feature that allows the coin details to be stored
     *
     */
    private fun addFeaturesAsUncollectedCoins(featureColl: FeatureCollection) {
        //iterate over feature collection and add markers
        Log.d(tag, "adding feature collection coins to database")
        val features = featureColl.features()

        if (features != null) { //if the featurecollection is not null
            for (f in features) {
                if (f != null) { //and feature f is not null within the featurecollcetion
                    val point: Geometry? = f.geometry()
                    if (point is Point) { // if the Feature is a Point then it is a coin

                        val coin = HashMap<String, Any?>() //
                        val currency = f.getStringProperty("currency") // the tag form of the currency eg. PENY
                        val currencyString = normaliseCurrencyName(currency) // the surface form of the currency eg. Penny
                        val valueString = f.getStringProperty("value")
                        val valueRounded = String.format("%.3f",valueString.toFloat())
                        val snippet = "$currencyString, value: $valueRounded"

                        coin.put("lat", point.latitude())
                        coin.put("long", point.longitude())
                        coin.put("currency", currency)
                        coin.put("value", valueString)
                        coin.put("symbol", f.getStringProperty("marker-symbol"))
                        coin.put("snippet", snippet)

                        userDB.collection("uncollectedCoins").document(f.getStringProperty("id")) // check firestore tutorial
                                .set(coin)
                                .addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }

                    }
                }
            }
        } else {
            Log.d(tag, "ERROR, feature list is null")
        }
        userDB.update("lastDownloadDate", todayDate) // set the user's date of last map download to today
        addUncollectedCoinsToMap()


    }

    /**
     * Removes the specified marker from the map.
     */
    private fun removeAllMarkers() {
        for (marker: Marker in map!!.markers) {
            map!!.removeMarker(marker)
        }
    }

    private fun addUncollectedCoinsToMap() {
        //iterate over feature collection and add markers

        val coinCollection = userDB.collection("uncollectedCoins")

        coinCollection.get()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "adding uncollected coins to map")
                        for (document in task.result!!) { // iterates over every coin entry in uncollectedCoins
                            if (document.id != "nullCoin") {

                                //retrieves the attributes from the coin document
                                val lat = document.get("lat").toString().toDouble()
                                val long = document.get("long").toString().toDouble()
                                val currency = document.get("currency").toString()
                                val symbol = document.get("symbol").toString()
                                val snippet = document.get("snippet").toString()

                                //find which icon to add based on currency and symbol(value) of the coin
                                val icon = findIcon(currency, symbol)

                                //creates a new marker using MarkerOptions and adds the marker to the map
                                map!!.addMarker(MarkerOptions()
                                        .position(LatLng(lat, long))
                                        .icon(icon)
                                        .snippet(snippet))
                            }
                        }
                    } else {
                        Log.d(tag, "Error getting documents: ", task.exception)
                    }
                }


    }

    /**
     * Function to determine which 'set' of marker coins are to be used.
     * findIcon takes the currency of the coin and the value of the coin and combines this
     * with the currently set user coin style.
     * The function finds the appropriate colour of the set (each currency has a different colour,
     * but this colour is dependent on the style chosen).
     * The correct marker is then returned from the typed array found in Resources.
     */
    private fun findIcon(currency: String, coinSymbol: String): Icon {
        var icons = resources.obtainTypedArray(R.array.original_quid_icons) //icons default to yellow if there is an issue in determining the set
        Log.d(tag, "adding coins of currency $currency to the map")
        when (currency) {
            "DOLR" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_dollar_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_dollar_icons))
                "party" -> icons = resources.obtainTypedArray(R.array.party_dollar_icons)
                "dark" -> icons = resources.obtainTypedArray(R.array.dark_dollar_icons)
            }
            "PENY" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_penny_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_penny_icons))
                "party" -> icons = resources.obtainTypedArray(R.array.party_penny_icons)
                "dark" -> icons = resources.obtainTypedArray(R.array.dark_penny_icons)
            }
            "SHIL" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_shilling_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_shilling_icons))
                "party" -> icons = resources.obtainTypedArray(R.array.party_shilling_icons)
                "dark" -> icons = resources.obtainTypedArray(R.array.dark_shilling_icons)
            }
            "QUID" -> when (iconStyle) {
                "original" -> icons = resources.obtainTypedArray(R.array.original_quid_icons)
                "pale" -> icons = resources.obtainTypedArray((R.array.pale_quid_icons))
                "party" -> icons = resources.obtainTypedArray(R.array.party_quid_icons)
                "dark" -> icons = resources.obtainTypedArray(R.array.dark_quid_icons)
            }
        }

        //the icon with the correct number is found from the set found above
        //(each set is a typed array, where a marker for a coin of value 1 is held in index 1)
        val iconFile = icons.getResourceId(Integer.parseInt(coinSymbol), 0)
        return IconFactory.getInstance(this).fromResource(iconFile)
    }

    private fun collectCoin(coinID: String, currency: String, coinValue: Float) {
        Log.d(tag, "collectCoin $coinID begins")

        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val document = task.result!!

                val goldBeforeCoin = (document.get("goldInBank").toString().toFloat())
                val size = Integer.parseInt(document.get("dailyCoinsCollected").toString())

                if (size <= 25) { // if there are less than 25 coins collected...
                    //...convert the coin to gold

                    val coinValueInGold = convertCoinToGold(currency, coinValue)

                    //... and add the total exchanged coin's gold total to the users gold total
                    userDB.update("goldInBank", (goldBeforeCoin + coinValueInGold))

                } else { // above 25 coins, add the coin (unexchanged) to the users wallet
                    Log.d(tag, "add to wallet: coinID - value: $coinValue currency: $currency")
                    val coin = HashMap<String, Any?>()
                    coin.put("currency", currency)
                    coin.put("value", coinValue)

                    //val coinDoc = userDB.collection("uncollectedCoins").document(document.id)
                    userDB.collection("wallet").document(coinID).set(coin).addOnFailureListener { e -> Log.w(tag, "Error adding document", e) }
                }

                //remove the collected coin from user's database collection "uncollectedCoins"

                userDB.collection("uncollectedCoins").document(coinID).delete()
                Log.d(tag, "updating user coins collected with " + (size + 1).toString())
                userDB.update("dailyCoinsCollected", size + 1)

                val totalCoins = Integer.parseInt(task.result!!.get("totalCoinsCollected").toString())
                userDB.update("totalCoinsCollected", totalCoins + 1)
                checkForCoinAchievements()

                // remove the collected coin from map
                removeAllMarkers() // remove all the current markers from the map
                addUncollectedCoinsToMap() //re-add all the uncollected coins stored in the database to the map
            }
        }

    }

    private fun checkForCoinAchievements() {

        userDB.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val coinsCollected = Integer.parseInt(task.result!!.get("totalCoinsCollected").toString())

                val newAchievement = coinsCollected == 1 || coinsCollected == 100 || coinsCollected == 1000
                Log.d(tag, "achievement: accessing user successful, total coins collected $coinsCollected, new achievement is $newAchievement")
                if (newAchievement) {
                    var achievement = ""

                    when (coinsCollected) {
                        1 -> achievement = "coinsCollected_BFC"//baby'sfirst coin
                        100 -> achievement = "coinsCollected_CE"//coin enthusiast
                        1000 -> achievement = "coinsCollected_RoAE" //root of all evil
                    }
                    Log.d(tag, "updating achievement $achievement")

                    userDB.collection("achievements").document(achievement)
                            .update("status", true).addOnFailureListener { _ ->
                                Log.d(tag, "achievement update failed!")
                            }

                    Toast.makeText(this, "New Achievement!", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    /**
     * Function to determine the value of the coin in gold, using the coin's currency
     * and the current exchange rate for that currency.
     */
    private fun convertCoinToGold(currency: String, value: Float): Float {
        val rate = getCoinExchangeRate(currency).toFloat()
        return rate * value
    }

    /**
     * Function to set the daily exchange rates for each currency from the geoJson data
     */
    private fun setDailyExchangeRates(geoJsonDataString: String) {
        val todaysRates = JSONObject(geoJsonDataString).getJSONObject("rates")

        shilRate = todaysRates.getDouble("SHIL")
        dolrRate = todaysRates.getDouble("DOLR")
        quidRate = todaysRates.getDouble("QUID")
        penyRate = todaysRates.getDouble("PENY")
    }


    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            if (!isNetworkAvailable()){
                Toast.makeText(this, "No network connection detected, please reconnect to collect coins", Toast.LENGTH_LONG).show()
            }
            originLocation = location
            setCameraPosition(location)
            checkCoinLocations(location) // check to see if a coin can be collected whenever the user's location changes
        }
    }

    private fun checkCoinLocations(location: Location) {
        var closeToCoin = false
        var coinID = ""

        userDB.collection("uncollectedCoins").get()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
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
                                    collectCoin(coinID, currency, value)
                                    closeToCoin = false
                                }
                            }
                        }
                    }
                }
    }

    //getters and setters for coin style
    fun getCoinStyle(): String {
        return iconStyle
    }

    fun setCoinStyle(newCoinStyle: String) {
        iconStyle = newCoinStyle
        Log.d(tag, "icon style is now $iconStyle")

        removeAllMarkers() // removes all markers of the previous coin style
        addUncollectedCoinsToMap() // re-adds all the markers with the new coin style
    }

    //sets the map newMapStyle
    fun setMapStyle(newMapStyle: String) {
        val mapStyleUrl = getMapStyleURL(newMapStyle) // gets the URL for the new map style using getMapStyleUrl
        map!!.setStyleUrl(mapStyleUrl) // sets the new map style using the mapbox URL
        Log.d(tag, "map newMapStyle is now $newMapStyle")
    }

    /**
     * Function to retrieve the style url for the specified style from Resources.
     * Returns the url for the style.
     */
    fun getMapStyleURL(style: String): String {
        var result = ""
        when (style) {
            "dark" -> result = resources.getString(R.string.dark_map_url)
            "original" -> result = resources.getString(R.string.original_map_url)
            "pale" -> result = resources.getString(R.string.pale_map_url)
        }
        return result
    }

    /**
     *  Function to change a currency from token form to surface form for the user display.
    */
    fun normaliseCurrencyName(currency: String) : String{
        var result = ""
        when (currency){
            "SHIL" -> result = "Shilling"
            "DOLR" -> result = "Dollar"
            "QUID" -> result = "Quid"
            "PENY" -> result = "Penny"
        }
        return result
    }

    fun getCoinExchangeRate(currency: String): Double {
        var rate = 0.0
        when (currency) {
            "DOLR" -> rate = dolrRate
            "PENY" -> rate = penyRate
            "QUID" -> rate = quidRate
            "SHIL" -> rate = shilRate
        }
        return rate
    }


//----------------------- all Location and permissions methods ---------------------------

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
            originLocation = lastLocation // set origin location as the last location
            setCameraPosition(lastLocation)
        } else // if last location does not exist
        {
            locationEngine.addLocationEngineListener(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initialiseLocationLayer() {
        if (mapView == null) {
            Log.d(tag, "mapView is null")
        } else {
            if (map == null) {
                Log.d(tag, "map is null")
            } else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin.apply {
                    setLocationLayerEnabled(true) // enables camera tracking location
                    cameraMode = CameraMode.TRACKING // tracks change in loc.
                    renderMode = RenderMode.NORMAL
                }
            }
        }
    }

    private fun setCameraPosition(location: Location) {
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
        Log.d(tag, "Permissions: $permissionsToExplain")
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted) {
            enableLocation()
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

    private fun isNetworkAvailable(): Boolean{
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
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
        currentMapStyle = settings.getString("lastMapStyle", "original")
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

        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit() // editor = makes preference changes
        //save the current set icon and map styles
        editor.putString("lastIconStyle", iconStyle)
        editor.putString("lastMapStyle", currentMapStyle)
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
