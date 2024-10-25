package com.sunnyweather.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.PlaceWeather
import com.sunnyweather.android.logic.model.allinfo

class WeatherViewModel : BaseViewModel() {
    private val locationLiveData = MutableLiveData<Location>()

    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    var placeAddress = ""

    val weatherLiveData = locationLiveData.switchMap { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }


    private val allPalceLiveData = MutableLiveData<List<Place>>()

    val weatherDataListLiveData = allPalceLiveData.switchMap { placeList ->
        Repository.refreshWeatherList(placeList)
    }

    fun refreshWeatherDataList() {
        allPalceLiveData.value = Repository.getAllPlace()
    }

    fun addPlace() {
        Repository.addPlace(Place(placeName,Location(locationLng,locationLat),placeAddress))
    }

    fun deletePlace(placeList: List<Place>) {
        for(place in placeList) {
            Repository.deletePlace(place)
        }
    }

    fun isPlaceExists(place: Place) = Repository.isPlaceExists(place)

}