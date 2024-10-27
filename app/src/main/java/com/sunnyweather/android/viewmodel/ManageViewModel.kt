package com.sunnyweather.android.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.PlaceWithWeather

class ManageViewModel : BaseViewModel() {

    private val allPlaceLiveData = MutableLiveData<List<Place>>()

    lateinit var placeList : MutableList<PlaceWithWeather>

    val weatherDataListLiveData = allPlaceLiveData.switchMap { placeList ->
        Repository.refreshWeatherList(placeList)
    }

    fun refreshWeatherDataList() {
        allPlaceLiveData.value = getAllPlace()
    }
}