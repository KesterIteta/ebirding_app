package za.edu.varsitycollege.featherfinder.network

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException

class Get_insert(context: Context) {
    private val helper = sqlLiteService(context)

    fun insert(bName:String, userLat:Double, userLon:Double, birdLat:Double, birdLon:Double):Long{
        val db = helper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("bName",bName)
        contentValues.put("userLat",userLat)
        contentValues.put("userLon",userLon)
        contentValues.put("birdLat",birdLat)
        contentValues.put("birdLon",birdLon)

        return try {
            db.insert("value", null, contentValues)
        } catch (e: SQLException) {
            -1
        } finally {
            db.close()
        }
    }

    @SuppressLint("Range")
    fun RetrieveData(): ArrayList<ObjData> {
        val data = ArrayList<ObjData>()
        val db = helper.readableDatabase

        val query = "SELECT * FROM value"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, null)
            while (cursor.moveToNext()) {
                val bName = cursor.getString(cursor.getColumnIndex("bName"))
                val uLat = cursor.getDouble(cursor.getColumnIndex("userLat"))
                val uLon = cursor.getDouble(cursor.getColumnIndex("userLon"))
                val bLat = cursor.getDouble(cursor.getColumnIndex("birdLat"))
                val bLon = cursor.getDouble(cursor.getColumnIndex("birdLon"))
                data.add(ObjData(bName, uLat, uLon, bLat, bLon))
            }
        } catch (e: SQLException) {

        } finally {
            cursor?.close()
            db.close()
        }
        return data
    }
}