package za.edu.varsitycollege.featherfinder.model

import com.google.gson.annotations.SerializedName

data class Observation(
    @SerializedName("speciesCode") val speciesCode: String,
    @SerializedName("comName") val comName: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("locName") val locName: String
)
