package com.sunnyweather.android.Utils

fun String.getContentAfterLastSpace(): String {
    val lastSpaceIndex = this.lastIndexOf(' ')
    return if (lastSpaceIndex!= -1) this.substring(lastSpaceIndex + 1) else this
}