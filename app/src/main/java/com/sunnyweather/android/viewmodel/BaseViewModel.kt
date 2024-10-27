package com.sunnyweather.android.viewmodel

import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

open class BaseViewModel : ViewModel() {
    // 提示城市
    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    fun isPlaceSaved() = Repository.isPlaceSaved()


    // 城市管理列表
    fun addPlace(place: Place) {
        Repository.addPlace(place)
    }

    fun deletePlace(place: Place) {
        Repository.deletePlace(place)
    }

    fun isPlaceExists(place: Place) = Repository.isPlaceExists(place)

    fun getAllPlace() = Repository.getAllPlace()
}