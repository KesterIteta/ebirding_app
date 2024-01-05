package za.edu.varsitycollege.featherfinder.ui.theme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import za.edu.varsitycollege.featherfinder.MapsActivity
import za.edu.varsitycollege.featherfinder.R

class Settings : AppCompatActivity() {
    var userLat:Double?= 0.0
    var userLon :Double?= 0.0
    var lat :Double?= 0.0
    var lon :Double?= 0.0
    var listBirname = mutableListOf<String>()
    var list = mutableListOf<store>()


    data class store(val birdName:String,val birdLat:Double,val birdLon:Double,  val userLat:Double, val userLon:Double)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val listview = findViewById<ListView>(R.id.listView)


        val adapters = ArrayAdapter(this, android.R.layout.simple_list_item_1, listBirname)

        // Set the adapter to the ListView
        listview.adapter = adapters
        dataValues(adapters)


        listview.setOnItemClickListener { parent, view, position, id ->
            val birdNameValue= listBirname[position]
            var found:Boolean= false
            for (element in list) {
                val birdName = element.birdName
                if (birdNameValue == birdName) {
                    userLat = element.userLat
                    userLon = element.userLon
                    lat = element.birdLat
                    lon = element.birdLon
                    found = true
                    break
                }
            }


            if (found) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("uLat", userLat)
                intent.putExtra("uLon", userLon)
                intent.putExtra("bLat", lat)
                intent.putExtra("bLon", lon)

                startActivity(intent)
            }
        }
    }
    fun dataValues(adapters: ArrayAdapter<String>):MutableList<String>{
        var listValue= mutableListOf<String>()

        val database = Firebase.firestore
        database.collection("Birds").get().addOnSuccessListener { value->
            for (element in value){

                val uid= element.getString("uid")

                if(uid == FirebaseAuth.getInstance().uid){
                    val bLat =element.getDouble("birdLat").toString().toDouble()
                    val bLon =element.getDouble("birdLon").toString().toDouble()
                    val uLat =element.getDouble("userLat").toString().toDouble()
                    val uLon =element.getDouble("userLon").toString().toDouble()
                    val bName= element.getString("birdName").toString()

                    val collection= store(bName, bLat, bLon,uLat,uLon)
                    listBirname.add(bName)
                    list.add(collection)

                }

            }
            adapters.notifyDataSetChanged()

        }

        return listValue
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }
}
