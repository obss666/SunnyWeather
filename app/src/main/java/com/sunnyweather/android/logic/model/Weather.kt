package com.sunnyweather.android.logic.model

data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)

data class PlaceWithWeather(
    val place: Place,
    val temperatureRange: DailyResponse.Temperature,
    val temperature: Float
)