package com.example.hslar.Services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationService (val context: Context) {

    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    var internalStorageService = InternalStorageService()
    //TODO: check permissions

    @SuppressLint("MissingPermission")
    fun getLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener {location: Location? ->

            Log.d("Main", "${location!!.longitude}")
            Log.d("Main", "${location!!.latitude}")
            var data = "${location!!.latitude}:${location!!.longitude}"
            internalStorageService.writeOnAFile(context,"location.txt", data)
        }
    }
    fun calculateDistanceFromTwoPoints(latitude: Double, longitude: Double, latitude1: Double, longitude1: Double): Float {

            var distance = createLocation(latitude, longitude).distanceTo(createLocation(latitude1, longitude1))
            return distance
    }
    fun calculateDistance(latitude1: Double, longitude1: Double): Float? {

        var data = internalStorageService.readOnFile(context,"location.txt")
        var lat = 0.0
        var long = 0.0

        return if(data!!.isNotEmpty()) {
            lat = data!!.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            var distance = createLocation(lat, long).distanceTo(createLocation(latitude1, longitude1))
            distance

        }else {
            null
        }

    }
    fun getYourLocation(): Location? {
        var data = internalStorageService.readOnFile(context,"location.txt")
        var lat: Double
        var long: Double

        return if(data!!.isNotEmpty()) {
            lat = data!!.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            var distance = createLocation(lat, long)
            distance

        }else {
            null
        }
    }

    fun createLocation(latitude: Double, longitude: Double): Location{
        var loc = Location("")
        loc.latitude = latitude
        loc.longitude = longitude
        return loc
    }
}