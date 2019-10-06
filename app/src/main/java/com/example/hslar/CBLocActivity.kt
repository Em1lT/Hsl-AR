package com.example.hslar

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_cbloc.*
import com.mapbox.geojson.Feature
import android.graphics.PointF




class CBLocActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var httpService: HttpService
    private lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService

    //lateinit var mapFragment: SupportMapFragment
    //lateinit var googleMap: GoogleMap


    private lateinit var mapBoxMap: MapboxMap
    private lateinit var myLocation: LatLng

    //var list = mutableListOf<CBStationModel>()
    var choice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_cbloc)

        //list = intent.extras.getSerializable("cb") as MutableList<CBStationModel>
        choice = intent.extras.getSerializable("button") as String

        locationService = LocationService(this)
        internalStorageService = InternalStorageService()
        getYourLocation()

        mapBoxCb.onCreate(savedInstanceState)
        mapBoxCb.getMapAsync(this)

        //mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //mapFragment.getMapAsync(this)

        if (choice == "return") {
            CBlocationTitle.setText(R.string.retutn_title)
        }
    }

    override fun onMapReady(map: MapboxMap) {
        mapBoxMap = map
        val cityBikes = CbList.cbList
        val latLngYou = LatLng(myLocation.latitude, myLocation.longitude)

        mapBoxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            val geoJson = GeoJsonOptions().withTolerance(0.4f)
            val symbolManager = SymbolManager(mapBoxCb, mapBoxMap, style, null, geoJson)

            style.addImage(
                "BIKE_STATION",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_bike_station))!!, true)
            //TODO: Popups for markers displaying data

            for(item in cityBikes) {

                val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())

                if (item.state == "Station on" && choice == "rent") {
                    if (item.bikesAvailable >= 1) {
                        var symbol6 = SymbolOptions()
                            .withLatLng(latLng)
                            .withIconImage("BIKE_STATION")
                            .withIconSize(1.3f)
                            .withDraggable(false)

                        symbolManager.create(symbol6)

                    }
                } else if (item.state == "Station on" && choice == "return") {
                    if (item.spacesAvailable >= 1 || item.allowDropoff) {
                        var symbol6 = SymbolOptions()
                            .withLatLng(latLng)
                            .withIconImage("BIKE_STATION")
                            .withIconSize(1.3f)
                            .withDraggable(false)

                        symbolManager.create(symbol6)
                    }
                }
            }

            style.addImage(
                "YOU",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_person_black_24dp))!!, true)

            var symbol2 = SymbolOptions()
                .withLatLng(latLngYou)
                .withIconImage("YOU")
                .withIconSize(1.3f)
                .withDraggable(false)

            symbolManager.create(symbol2)

        }

        val position = CameraPosition.Builder()
            .target(LatLng(latLngYou.latitude, latLngYou.longitude)).zoom(16.5).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)

        /*
        googleMap = map


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
        val latLng = LatLng(60.258846, 24.846266)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
        googleMap.addMarker(
            MarkerOptions().position(latLng).title("You")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
        )

         */
    }

    private fun getYourLocation(){
        var data = internalStorageService.readOnFile(this,"location.txt")
        var lat: Double
        var long: Double
        if(data!!.isNotEmpty()) {
            lat = data!!.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            myLocation = LatLng(lat, long)

        }
    }
}