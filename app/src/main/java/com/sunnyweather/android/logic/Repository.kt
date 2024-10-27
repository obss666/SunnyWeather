package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.dao.PlaceDatabaseHelper
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.PlaceWithWeather
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()

            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(
                    realtimeResponse.result.realtime,
                    dailyResponse.result.daily
                )
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    fun refreshWeatherList(placeList: List<Place>) = fire(Dispatchers.IO) {
        coroutineScope {
            val weatherDataList = mutableListOf<PlaceWithWeather>()
            for (place in placeList) {
                val location = place.location
                val lng = location.lng
                val lat = location.lat
                val deferredRealtime = async { SunnyWeatherNetwork.getRealtimeWeather(lng, lat) }
                val deferredDaily = async { SunnyWeatherNetwork.getDailyWeather(lng, lat) }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                    weatherDataList.add(
                        PlaceWithWeather(place, weather.daily.temperature[0], weather.realtime.temperature)
                    )
                }
            }
            if (weatherDataList.size == placeList.size) {
                Result.success(weatherDataList)
            } else {
                Result.failure(
                    RuntimeException("Request incomplete")
                )
            }
        }
    }


    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()



    fun addPlace(place: Place) = PlaceDatabaseHelper.insertPlace(place)

    fun getAllPlace() = PlaceDatabaseHelper.getAllPlaces()

    fun deletePlace(place: Place) = PlaceDatabaseHelper.deletePlace(place)

    fun isPlaceExists(place: Place) = PlaceDatabaseHelper.isPlaceExists(place)
}