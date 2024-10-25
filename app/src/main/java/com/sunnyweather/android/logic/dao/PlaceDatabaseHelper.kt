package com.sunnyweather.android.logic.dao

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.sunnyweather.android.ui.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Place

object PlaceDatabaseHelper: SQLiteOpenHelper(SunnyWeatherApplication.context, "place_database", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE places (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, lat TEXT, lng TEXT, address TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun isPlaceExists(place: Place): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM places WHERE name =? AND lat =? AND lng =? AND address =?",
            arrayOf(place.name, place.location.lat, place.location.lng, place.address)
        )
        val exists = cursor.moveToNext()
        cursor.close()
        return exists
    }

    fun insertPlace(place: Place): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("name", place.name)
            put("lat", place.location.lat)
            put("lng", place.location.lng)
            put("address", place.address)
        }
        Log.d("11111", "insertPlace")

        return db.insert("places", null, contentValues)
    }

    fun deletePlace(place: Place): Int {
        val db = writableDatabase
        return db.delete("places", "name =? AND lat =? AND lng =? AND address =?",
            arrayOf(place.name, place.location.lat, place.location.lng, place.address))
    }

    @SuppressLint("Range")
    fun getAllPlaces(): List<Place> {
        val places = mutableListOf<Place>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM places", null)
        while (cursor.moveToNext()) {
            val place = Place(
                cursor.getString(cursor.getColumnIndex("name")),
                Location(
                    cursor.getString(cursor.getColumnIndex("lng")),
                    cursor.getString(cursor.getColumnIndex("lat"))
                ),
                cursor.getString(cursor.getColumnIndex("address"))
            )
            places.add(place)
        }
        cursor.close()
        return places
    }
}