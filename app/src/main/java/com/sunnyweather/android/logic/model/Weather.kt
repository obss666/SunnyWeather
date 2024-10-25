package com.sunnyweather.android.logic.model

data class allinfo(val place: Place, val weather: Weather)

data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily)

data class PlaceWeather(
    val placename: String,
    val temperatureRange: DailyResponse.Temperature,
    val temperature: Float
)