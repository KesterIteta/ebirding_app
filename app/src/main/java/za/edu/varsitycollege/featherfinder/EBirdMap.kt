package za.edu.varsitycollege.featherfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class EBirdMap : AppCompatActivity() {

    private val apiKey = "5dkjl6aotecc"
    private val baseUrl = "https://api.ebird.org/v2"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ebird_map)

        // Initialize the map view
        val mapView = findViewById<MapView>(R.id.map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Set the map center and zoom level
        val mapController = mapView.controller as IMapController
        mapController.setZoom(15.0)
        mapController.setCenter(GeoPoint(-33.918861, 18.423300))

        // Fetch bird observations from eBird API
        val location = "-33.918861,18.423300"
       // val observations = eBirdAPI.getObservationsByLocation(location)

        // Add markers for each bird observation
       /* for (observation in observations) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(observation.lat, observation.lng)
            marker.title = observation.comName
            marker.snippet = observation.locName // Displaying location name as snippet
            mapView.overlays.add(marker)
        }*/
    }
}

