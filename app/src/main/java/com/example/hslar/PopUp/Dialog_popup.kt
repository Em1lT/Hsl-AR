package com.example.hslar.PopUp

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


@SuppressLint("ValidFragment")
class Dialog_popup(val location: Location, val location1: Location) : DialogFragment(), OnMapReadyCallback {

    lateinit var googleMap: GoogleMap
    lateinit var mapFragment: SupportMapFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

            val view = inflater.inflate(R.layout.pop_up, container, false)
            mapFragment = fragmentManager!!.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        return view
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val latLng = LatLng(location.latitude, location.longitude)
        val latLng1 = LatLng(location1.latitude, location1.longitude)

        googleMap.addMarker(MarkerOptions().position(latLng).title("You").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
        googleMap.addMarker(MarkerOptions().position(latLng1).title("Stop"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
    }

    override fun onDestroy() {
        fragmentManager!!.beginTransaction().remove(mapFragment).commit()

        super.onDestroy()
    }
}