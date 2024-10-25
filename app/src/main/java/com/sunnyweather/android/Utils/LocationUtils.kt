package com.sunnyweather.android.Utils

import android.content.Context
import androidx.core.content.ContextCompat

object LocationUtils {
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                coarseLocationPermission == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}