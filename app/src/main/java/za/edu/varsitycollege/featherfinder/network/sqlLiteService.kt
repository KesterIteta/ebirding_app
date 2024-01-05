package za.edu.varsitycollege.featherfinder.network
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class sqlLiteService(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "Observation.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = "CREATE TABLE value (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "bName TEXT," +
                "userLat REAL," +
                "userLon REAL," +
                "birdLat REAL," +
                "birdLon REAL" +
                ")"
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS LocationData")
        onCreate(db)
    }
}
