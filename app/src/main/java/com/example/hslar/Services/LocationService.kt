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
}