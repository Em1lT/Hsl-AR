package com.example.hslar.Services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import android.widget.Toast
import com.example.hslar.MainActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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