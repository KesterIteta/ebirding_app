package za.edu.varsitycollege.featherfinder
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.DirectionsApi
import com.google.maps.android.SphericalUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import android.widget.Toast
import com.google.maps.GeoApiContext

import za.edu.varsitycollege.featherfinder.ui.theme.Settings

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private val PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var edtNum:EditText
    private lateinit var sub: Button
    private lateinit var toggleBtn: ToggleButton

    // Storing the selected unit of measurement
    private var unitOfMeasurement = "kilometers"
    private var distanceValue = 10.0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        edtNum =findViewById(R.id.edtNum)
        sub = findViewById(R.id.subm)
        toggleBtn = findViewById(R.id.toggle_button)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val setting = findViewById<Button>(R.id.setting)

        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(policy)

        sub.setOnClickListener() {
            locations()
        }
        toggleBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
            {
                // If user has selected miles
                unitOfMeasurement = "miles"
            } else {
                // User has selected kilometers
                unitOfMeasurement = "kilometers"
            }
        }

        edtNum.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
                // Do nothing
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                // Parse the distance value from the EditText and store it in a variable
                distanceValue = s.toString().toDoubleOrNull() ?: 0.0
            }
            override fun afterTextChanged(s: Editable?)
            {
                // Do nothing
            }
        })

        setting.setOnClickListener()
        {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true

        //Giving permission for access
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }

        // Enable the user's location on the map
        map.isMyLocationEnabled = true
        var lat: Double = 0.0
        var lng: Double = 0.0
        //Using fused location to get user listen location
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null)
            {

                try {
                    val location = LatLng(location.latitude, location.longitude)
                    val marker = MarkerOptions().position(location).title("My location")
                    map.addMarker(marker)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

                    val userLat:Double = intent.getDoubleExtra("uLat", 0.0)
                    val userLon = intent.getDoubleExtra("uLon", 0.0)
                    val lat = intent.getDoubleExtra("bLat", 0.0)
                    val lon = intent.getDoubleExtra("bLon", 0.0)
                    val km =findViewById<TextView>(R.id.km)
                    val distance =findViewById<TextView>(R.id.distance)
                    val fetch = Fetch(map,km,distance)
                    val apiKey = "AIzaSyCcc-f58IlQILFR7qmbRWhgAHadQCYgylY"
                    val contexts = GeoApiContext.Builder().apiKey(apiKey).build()
                    val result: DirectionsResult? = DirectionsApi.newRequest(contexts)
                        .mode(TravelMode.DRIVING)
                        .origin("${userLat},${userLon}")
                        .destination("${lat},${lon}")
                        .await()

                    if (result!!.routes.isNotEmpty()){

                        val route = result.routes[0]

                        km.text="Distance: ${(route.legs[0].distance.humanReadable).toString()}"
                        distance.text="Duration: ${(route.legs[0].distance.inMeters/1000.0).toString()+" minutes"}"
                    }

                    fetch.lats = location.latitude.toString()
                    fetch.lng = location.longitude.toString()
                    fetch.iULast = userLat
                    fetch.iULon = userLon
                    fetch.iBLast = lat
                    fetch.iBLon = lon
                    fetch.cxt = this
                    fetch.execute(location)
                } catch (e: Exception) {
                    e.printStackTrace()
                   Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Define a function to calculate the distance between two LatLng objects
    fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        return SphericalUtil.computeDistanceBetween(point1, point2)
    }
    // Define a function to convert a distance value from kilometers to miles
    fun kilometersToMiles(kilometers: Double): Double {
        return kilometers / 1.60934
    }
    // Define a function to convert a distance value from miles to kilometers
    fun milesToKilometers(miles: Double): Double
    {
        return miles * 1.60934
    }

    private fun locations() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {

                val location = LatLng(location.latitude, location.longitude)
                val marker = MarkerOptions().position(location).title("My location")
                map.addMarker(marker)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                val km =findViewById<TextView>(R.id.km)
                val distance =findViewById<TextView>(R.id.distance)
                val userLat: Double = intent.getDoubleExtra("uLat", 0.0)
                val userLon = intent.getDoubleExtra("uLon", 0.0)
                val lat = intent.getDoubleExtra("bLat", 0.0)
                val lon = intent.getDoubleExtra("bLon", 0.0)
                //Fetching the map
                val fetch = Fetch(map,km,distance)
                fetch.lats = location.latitude.toString()
                fetch.lng = location.longitude.toString()
                fetch.iULast = userLat
                fetch.iULon = userLon
                fetch.iBLast = lat
                fetch.iBLon = lon
                fetch.distance = edtNum.text.toString().toDouble()

                fetch.cxt = this
                fetch.execute(location)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
//////////////////////////////////////////////////// END OF CLASS ////////////////////////////////////////////////////
























