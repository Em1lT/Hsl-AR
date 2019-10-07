package com.example.hslar.Services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * 07.09.2019
 * Used to get the location using Google Fused Location. Also used to calculate distances between different things
 *
 */
class LocationService (val context: Context) {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var internalStorageService = InternalStorageService()
    //TODO: check permissions

    @SuppressLint("MissingPermission")
    fun getLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener {location: Location? ->

            Log.d("Main", "${location!!.longitude}")
            Log.d("Main", "${location.latitude}")
            val data = "${location.latitude}:${location.longitude}"
            internalStorageService.writeOnAFile(context,"location.txt", data)
        }
    }
    fun calculateDistanceFromTwoPoints(latitude: Double, longitude: Double, latitude1: Double, longitude1: Double): Float {

        return createLocation(latitude, longitude).distanceTo(createLocation(latitude1, longitude1))
    }
    fun calculateDistance(latitude1: Double, longitude1: Double): Float? {

        val data = internalStorageService.readOnFile(context,"location.txt")
        val lat: Double
        val long: Double

        return if(data!!.isNotEmpty()) {
            lat = data.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            val distance = createLocation(lat, long).distanceTo(createLocation(latitude1, longitude1))
            distance

        }else {
            null
        }

    }
    fun getYourLocation(): Location? {
        val data = internalStorageService.readOnFile(context,"location.txt")
        val lat: Double
        val long: Double

        return if(data!!.isNotEmpty()) {
            lat = data.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            val distance = createLocation(lat, long)
            distance

        }else {
            null
        }
    }

    fun createLocation(latitude: Double, longitude: Double): Location{
        val loc = Location("")
        loc.latitude = latitude
        loc.longitude = longitude
        return loc
    }
}