package com.example.csd3156project2026

import com.google.android.gms.maps.model.LatLng

data class MarkerData(
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}