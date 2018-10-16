package com.example.sophie.coinzapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Geometry
import com.mapbox.mapboxsdk.Mapbox
import com.google.gson.JsonObject
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.light.Position
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
//import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions



import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener, PermissionsListener{
    //

    private var mapView: MapView? = null
    private var map : MapboxMap? = null
    private val tag = "MainActivity"

    private var downloadDate = "" // is in YYYY/MM/DD format
    private val todayDate = SimpleDateFormat("YYYY/MM/dd").format(Calendar.getInstance().time)

    private var preferencesFile = "MyPrefsFile"

    private lateinit var originLocation: Location // where the current location is stored at all times
    private lateinit var permissionsManager : PermissionsManager // removes code required for permissions
    private lateinit var locationEngine : LocationEngine // component that gives user location
    private lateinit var locationLayerPlugin : LocationLayerPlugin // provides location awareness to mobile - icons representation of user location

    //Coinz impl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Mapbox access token

        setContentView(R.layout.activity_main)
        Mapbox.getInstance(this, "pk.eyJ1Ijoic29waGllbGVhdmVyIiwiYSI6ImNqbXRkdXdkdzE5NXozcWw4cHNtN2RxbjAifQ.qdD_mPQW_dthnmphOmiZRw")
        mapView = findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView?.getMapAsync (this)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    @SuppressLint("MissingPermission")
    public override fun onStart() {
        super.onStart()
//        if (PermissionsManager.areLocationPermissionsGranted(this)){
//            locationEngine?.requestLocationUpdates()
//            locationLayerPlugin?.onStart()


//        }

        //restore user preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        downloadDate = settings.getString("lastDownloadDate", "")
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$downloadDate'")
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
        //locationEngine?.removeLocationUpdates()
        //        //locationLayerPlugin?.onStop()

        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")

        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val editor = settings.edit() // editor = makes pref changes
        editor.putString("lastDownloadDate", downloadDate)
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
            //Log.d(tag, "today date = $todayDate");
            map?.uiSettings?.isCompassEnabled = true // set ui options
            map?.uiSettings?.isZoomControlsEnabled = true

            enableLocation()

            //**DOWNLOAD GEOJSON MAP FOR THE DAY

            val testGeoJsonUrl = "http://homepages.inf.ed.ac.uk/stg/coinz/$todayDate/coinzmap.geojson"
                //Log.d(tag, "URL is $testGeoJsonUrl")
            val geoJsonDataString = DownloadFileTask(caller = DownloadCompleteRunner).execute(testGeoJsonUrl).get() // TODO check on listener functionality

            val source = GeoJsonSource("geojson", geoJsonDataString) // create new GeoJsonSource from string json map data
            mapboxMap.addSource(source) // add source to the map
                //mapboxMap.addLayer(var layer =  LineLayer("geojson")

            val featureCollection: FeatureCollection = FeatureCollection.fromJson(geoJsonDataString)

            val features = featureCollection.features()
            if (features != null) {
                for (f in features) {
                    val point : Geometry ?= f.geometry()
                    if (point is Point) {
                        mapboxMap.addMarker(MarkerOptions().position(LatLng(point.latitude(), point.longitude())))
                    }
                }
            } else {
                Log.d(tag, "ERROR,$ feature list is null") //implication?
            }
        }
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
            interval = 5000 // preferably every 5 seconds !!  TODO rewrite comments
            fastestInterval = 1000 // at most every second
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

    private fun setCameraPosition(location: Location) {
        // set camera location
        val latlng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(location) // changed from originLocation
        }
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
