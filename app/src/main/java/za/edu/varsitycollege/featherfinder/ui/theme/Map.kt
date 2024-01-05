package za.edu.varsitycollege.featherfinder.ui.theme

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import za.edu.varsitycollege.featherfinder.R

class Map : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var mapView:MapView
    private val eBirdApiKey ="5dkjl6aotecc";
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val birdLocation = LatLng(-33.924870, 18.424055)
        map.addMarker(MarkerOptions().position(birdLocation).title("cape town bird"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(birdLocation,15f))

        fetchBirdSightingAndDisplayOnMap()
    }
    fun fetchBirdSightingAndDisplayOnMap() {

    }
}