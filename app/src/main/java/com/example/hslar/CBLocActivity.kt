package com.example.hslar

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_cbloc.*
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import android.support.v4.graphics.drawable.DrawableCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.res.ResourcesCompat
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import com.mapbox.mapboxsdk.annotations.Icon
import kotlin.math.roundToInt


class CBLocActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService

    private lateinit var mapBoxMap: MapboxMap
    private lateinit var myLocation: LatLng

    var choice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_cbloc)

        choice = intent.extras.getSerializable("button") as String

        locationService = LocationService(this)
        internalStorageService = InternalStorageService()
        getYourLocation()

        mapBoxCb.onCreate(savedInstanceState)
        mapBoxCb.getMapAsync(this)

        if (choice == "return") {
            CBlocationTitle.setText(R.string.retutn_title)
        }
    }

    override fun onMapReady(map: MapboxMap) {
        mapBoxMap = map
        val cityBikes = CbList.cbList
        val latLngYou = LatLng(myLocation.latitude, myLocation.longitude)

        mapBoxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            for(item in cityBikes) {
                val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())

                if (item.state == "Station on") {
                        if (item.bikesAvailable >= 1 && choice == "rent") {
                            addMarkerCb(
                                latLng,
                                item.name,
                                "${getString(R.string.bikes_available)} ${item.bikesAvailable}\n" +
                                        "${distanceAdapter(item.dist)} ${getString(R.string.cb_dist)}",
                                R.drawable.ic_bike_station
                            )
                        } else if (choice == "return") {
                            if (item.spacesAvailable >= 1 || item.allowDropoff) {
                                addMarkerCb(
                                    latLng,
                                    item.name,
                                    "${getString(R.string.spaces_available)} ${item.spacesAvailable}\n" +
                                            "${getString(R.string.cb_dropoff)}\n" +
                                            "${distanceAdapter(item.dist)} ${getString(R.string.cb_dist)}",
                                    R.drawable.ic_bike_station
                            )
                        }
                    }
                }
            }
            addMarkerYou(latLngYou, getString(R.string.you), R.drawable.ic_person_black_24dp)
        }

        val position = CameraPosition.Builder()
            .target(LatLng(latLngYou.latitude, latLngYou.longitude)).zoom(16.5).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)

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

    private fun addMarkerCb(latLng: LatLng, title: String, snippet: String, drawable: Int) {
        mapBoxMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(drawableToIcon(this, drawable, resources.getColor(R.color.black)))
        )
    }

    private fun addMarkerYou(latLng: LatLng, title: String, drawable: Int) {
        mapBoxMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(drawableToIcon(this, drawable, resources.getColor(R.color.black)))
        )
    }

    fun drawableToIcon(context: Context, @DrawableRes id: Int, @ColorInt colorRes: Int): Icon {
        val vectorDrawable =
            ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme())
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        DrawableCompat.setTint(vectorDrawable, colorRes)
        vectorDrawable.draw(canvas)
        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    private fun distanceAdapter(distance: String) : String {
        var dist: String
        if(distance.toDouble() > 1000){
            dist = "${"%.2f".format(distance.toDouble() / 1000)} km"
        } else {
            dist = "${(distance.toDouble()).roundToInt()} ${getString(R.string.dist_meter)}"
        }
        return dist
    }
}