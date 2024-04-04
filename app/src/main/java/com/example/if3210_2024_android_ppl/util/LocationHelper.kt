package com.example.if3210_2024_android_ppl.util

import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import java.util.Locale

import android.Manifest
import android.content.Context
import com.google.android.gms.location.LocationServices

class LocationHelper(private val context: Context) {

    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLocationDetails(callback: (locationName: String?, latitude: Double?, longitude: Double?) -> Unit) {
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle the case where permissions are not granted
            callback.invoke(null, null, null)
            return
        }
        task.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val address = addresses?.get(0)
                        val locationName = address?.getAddressLine(0)
                        val latitude = location.latitude
                        val longitude = location.longitude
                        callback.invoke(locationName, latitude, longitude)
                    } else {
                        // If no address found, return null values
                        callback.invoke(null, null, null)
                    }
                }
            } else {
                // If location is null, return null values
                callback.invoke(null, null, null)
            }
        }
    }

}