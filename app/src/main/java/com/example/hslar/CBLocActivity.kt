package com.example.hslar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.hslar.Services.HttpService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_cbloc.*
import java.io.Serializable


class CBLocActivity : AppCompatActivity(), OnMapReadyCallback, Serializable {

    lateinit var httpService: HttpService
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap

    //var list = mutableListOf<CBStationModel>()
    var choice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cbloc)

        //list = intent.extras.getSerializable("cb") as MutableList<CBStationModel>
        choice = intent.extras.getSerializable("button") as String

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (choice == "return") {
            //CBlocationTitle.setText(R.string.retutn_title)
        }

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val cityBikes = CbList.cbList

        //TODO: Check your own location, and get map closer to position

        for(item in cityBikes) {
                if (item.state == "Station on" && choice == "rent") {
                    if (item.bikesAvailable >= 1) {
                        val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())
                        googleMap.addMarker(
                            MarkerOptions().position(latLng).title(item.name)
                                .snippet("Bikes available: ${item.bikesAvailable}")
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                                )
                        )
                    }
                } else if (item.state == "Station on" && choice == "return") {
                    if (item.spacesAvailable >= 1 || item.allowDropoff) {
                        val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())
                        googleMap.addMarker(
                            MarkerOptions().position(latLng).title(item.name)
                                .snippet("Spaces available: ${item.spacesAvailable} and/or drop off allowed")
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                                )
                        )
                    }
                }
        }
        //  LatLng(60.258846, 24.846266), Leiritie 1, for placeholder until own location can be found
        //val latLng = LatLng(cityBikes[26].lat.toDouble(), cityBikes[26].lon.toDouble())
        val latLng = LatLng(60.258846, 24.846266)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
        googleMap.addMarker(
            MarkerOptions().position(latLng).title("You")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
        )
    }
}