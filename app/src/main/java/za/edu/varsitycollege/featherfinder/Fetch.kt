package za.edu.varsitycollege.featherfinder
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class BirdMarker(val latLng: LatLng, val birdName: String)

class Fetch (private val googleMap: GoogleMap,Km:TextView,Distances:TextView) : AsyncTask<LatLng, Void, List<BirdMarker>>() {
    var lats: String? = ""
    var lng: String? = ""
    var iULast: Double? = 0.0
    var iULon: Double? = 0.0
    var iBLast: Double? = 0.0
    var iBLon: Double? = 0.0
    var distance:Double =0.0
    var kmtxt:TextView=Km
    var distancetxt:TextView =Distances
    lateinit var cxt: Context
    var km:String=""
    var distanceMap:String=""
    var routesPolyline: Polyline? = null
    override fun doInBackground(vararg params: LatLng): List<BirdMarker> {
        val userLocation = params[0]
        val birdMarkers = mutableListOf<BirdMarker>()
        if(distance == 0.0){
            distance = 35.0
        }
        try {

            val eBirdAPIUrl =
                "https://api.ebird.org/v2/data/obs/geo/recent" +
                        "?lat=${userLocation.latitude}&lng=${userLocation.longitude}" +
                        "&dist=${distance} "

            val url = URL(eBirdAPIUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("X-eBirdApiToken", "5dkjl6aotecc")

            val inputStream: InputStream = connection.inputStream
            val response = InputStreamReader(inputStream)
            val reader = BufferedReader(response)
            val data = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                data.append(line)
            }

            val jsonArray = JSONArray(data.toString())

            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val latitude = jsonObject.getDouble("lat")
                val longitude = jsonObject.getDouble("lng")
                val birdName = jsonObject.getString("comName")
                birdMarkers.add(BirdMarker(LatLng(latitude, longitude), birdName))
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return birdMarkers
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onPostExecute(result: List<BirdMarker>) {
        super.onPostExecute(result)

        for (birdMarker in result ) {
            val markerOptions = MarkerOptions()
                .position(birdMarker.latLng)
                .title("Bird Name: ${birdMarker.birdName}")

            // Setting a default marker icon
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = birdMarker
        }


        if(iULast != 0.0){

            val apiKey = "AIzaSyCcc-f58IlQILFR7qmbRWhgAHadQCYgylY"
            val context = GeoApiContext.Builder().apiKey(apiKey).build()
            val request = DirectionsApi.getDirections(
                context,
                "${iULast},${iULon}",
                "${iBLast},${iBLon}"
            ).mode(TravelMode.DRIVING)

            val directionsResult: DirectionsResult = request.await()

            // Create a Polyline Options to represent the route
            val lineOptions = PolylineOptions()
                .width(5f)
                .color(Color.RED)

            val legs = directionsResult.routes[0].legs
            for (leg in legs) {
                val steps = leg.steps
                for (step in steps) {
                    val polyline = step.polyline
                    val decodedPolyline = PolyUtil.decode(polyline.encodedPath)
                    lineOptions.addAll(decodedPolyline)
                }
            }

            // Draw the route on the map
            routesPolyline?.remove()
            routesPolyline = googleMap.addPolyline(lineOptions)
        }


        googleMap.setOnMarkerClickListener { clickedMarker ->

            if (clickedMarker.tag is BirdMarker  ) {
                val birdMarker = clickedMarker.tag as BirdMarker

                val userLocation = LatLng(lats.toString().toDouble(), lng.toString().toDouble())
                val apiKey = "AIzaSyCcc-f58IlQILFR7qmbRWhgAHadQCYgylY"
                val context = GeoApiContext.Builder().apiKey(apiKey).build()
                val request = DirectionsApi.getDirections(
                    context,
                    "${userLocation.latitude},${userLocation.longitude}",
                    "${birdMarker.latLng.latitude},${birdMarker.latLng.longitude}"
                ).mode(TravelMode.DRIVING)
                val eBirdName:String =birdMarker.birdName
                showDialogs(eBirdName,userLocation.latitude, userLocation.longitude,birdMarker.latLng.latitude,birdMarker.latLng.longitude)
                val directionsResult: DirectionsResult = request.await()
                val result: DirectionsResult? = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin("${userLocation.latitude},${userLocation.longitude}")
                    .destination("${birdMarker.latLng.latitude},${birdMarker.latLng.longitude}")
                    .await()

                if (result!!.routes.isNotEmpty()){

                    val route = result.routes[0]

                    km  =(route.legs[0].distance.humanReadable).toString()
                    distanceMap =(route.legs[0].distance.inMeters/1000.0).toString()+" minutes"
                    distancetxt.text="Duration: $distanceMap"
                    kmtxt.text="Distance: $km"
                }
                val lineOptions = PolylineOptions()
                    .width(5f)
                    .color(Color.RED)

                val legs = directionsResult.routes[0].legs

                for (leg in legs)
                {
                    val steps = leg.steps
                    for (step in steps)
                    {
                        val polyline = step.polyline
                        val decodedPolyline = PolyUtil.decode(polyline.encodedPath)
                        lineOptions.addAll(decodedPolyline)
                    }
                }

                // Draw the route on the map
                routesPolyline?.remove()
                routesPolyline = googleMap.addPolyline(lineOptions)
            }
            true // Indicates that the listener has consumed the event
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showDialogs(bName:String, userLat:Double, userLon:Double, birdLat:Double, birdLon:Double){

        val inflater = LayoutInflater.from(cxt)
        val dialogDesign = inflater.inflate(R.layout.dialog,null)

        val direction = dialogDesign.findViewById<Button>(R.id.direction)
        val save = dialogDesign.findViewById<Button>(R.id.save)
        val cancel = dialogDesign.findViewById<Button>(R.id.cancel)

        val builder =AlertDialog.Builder(cxt)
        builder.setView(dialogDesign)
        val dialog = builder.create()
        dialog.show()
        direction.setOnClickListener(){

            dialog.dismiss()
        }
        save.setOnClickListener(){

            val uid = FirebaseAuth.getInstance().uid.toString()
            val hostpot =HotSpot(bName,birdLat,birdLon,userLat,userLon,uid)
            val database = Firebase.firestore
            val time = System.currentTimeMillis()
            database.collection("Birds").document(time.toString()).set(hostpot).addOnSuccessListener {
                Toast.makeText(cxt,"Saved successfully",Toast.LENGTH_LONG).show()
            }
                .addOnFailureListener {
                    Toast.makeText(cxt,"fail to save",Toast.LENGTH_LONG).show()
                }
            dialog.dismiss()
        }
        cancel.setOnClickListener(){
            dialog.dismiss()
        }
    }

    data class  HotSpot(val birdName:String,val birdLat:Double,val birdLon:Double,  val userLat:Double, val userLon:Double,val uid:String)
}
