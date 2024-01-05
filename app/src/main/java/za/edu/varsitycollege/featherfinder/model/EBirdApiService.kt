package za.edu.varsitycollege.featherfinder.model

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EBirdApiService {
    val latitude: Double
    val longitude: Double

    @GET("data/obs/geo/recent")
fun getBirdSightings(
    @Query("lat") latitude:Double,
    @Query("lng") longitude:Double,
    @Query("dist") distance:Double,

): Call<List<EBirdApiService>>

    data class BirdSighting(
        val latitude: Double,
        val longitude: Double,
        val birdName: String
    )



}