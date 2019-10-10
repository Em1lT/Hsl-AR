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


class CBLocActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var internalStorageService: InternalStorageService

    private lateinit var mapBoxMap: MapboxMap
    private lateinit var myLocation: LatLng
    private lateinit var locationService: LocationService

    private var choice: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_cbloc)

        choice = intent?.extras?.getSerializable("button") as String

        locationService = LocationService(this)
        internalStorageService = InternalStorageService()

        mapBoxCb.onCreate(savedInstanceState)
        mapBoxCb.getMapAsync(this)

        // Refreshes location.txt file to get current location.
        locationService.getLocation()
        myLocation = LatLng(locationService.getYourLocation())

    }

    // Creates the mapboxmap and sets its style and markers.
    override fun onMapReady(map: MapboxMap) {
        mapBoxMap = map

        val cityBikes = CbList.cbList
        val scooters = CbList.scooterList

        val latLngYou = LatLng(myLocation.latitude, myLocation.longitude)

        mapBoxMap.setStyle(Style.MAPBOX_STREETS) {

            // Adds markers depending on users choice.
            if (choice == "rent" || choice == "return") {
                for (item in cityBikes) {
                    val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())

                    // Adds only bike stations that have 1 or more free bikes to use.
                    if (item.state == "Station on") {
                        if (item.bikesAvailable >= 1 && choice == "rent") {
                            addMarker(
                                latLng,
                                item.name,
                                "${getString(R.string.bikes_available)} ${item.bikesAvailable}\n" +
                                        "${distanceAdapter(item.dist)} ${getString(R.string.cb_dist)}",
                                R.drawable.ic_bike_station
                            )
                        } else if (choice == "return") {
                            // Adds only bike stations that have 1 or more free spaces, or have allowed drop off.
                            if (item.spacesAvailable >= 1 || item.allowDrop) {
                                addMarker(
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
            } else if (choice == "scooter") {
                for(item in scooters) {
                    // Adds only Voi scooters that are not being ridden and have more than 50% battery left.
                    if (item.status == "ready" && item.battery > 49) {
                        val latLng = LatLng(item.lat.toDouble(), item.lng.toDouble())
                        addMarker(
                            latLng,
                            "${item.name} ${getString(R.string.cs_pop_title)}",
                            "${getString(R.string.cs_battery)} ${item.battery}%\n" +
                                    "${distanceAdapter(item.distance)} ${getString(R.string.cb_dist)}",
                            R.drawable.ic_city_scooter
                        )
                    }
                }
            }
            addMarkerYou(latLngYou, getString(R.string.you))
        }
        val position = CameraPosition.Builder()
            .target(LatLng(latLngYou.latitude, latLngYou.longitude)).zoom(17.0).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000)
    }

    // Adds a marker with a snippet to mapboxmap.
    private fun addMarker(latLng: LatLng, title: String, snippet: String, drawable: Int) {
        mapBoxMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(drawableToIcon(this, drawable, resources.getColor(R.color.black)))
        )
    }

    // Adds a marker with out a snippet and set icon for device location.
    private fun addMarkerYou(latLng: LatLng, title: String) {
        mapBoxMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(drawableToIcon(this, R.drawable.ic_person_black_24dp, resources.getColor(R.color.black)))
        )
    }

    // Transforms a drawable object to an icon that can be used on mapboxmap as a marker.
    private fun drawableToIcon(context: Context, @DrawableRes id: Int, @ColorInt colorRes: Int): Icon {
        val vectorDrawable =
            ResourcesCompat.getDrawable(context.resources, id, context.theme)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, colorRes)
        vectorDrawable.draw(canvas)
        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    // Formats the distance float depending is it over 1000 or not.
    private fun distanceAdapter(distance: String) : String {
        val dist: String
        if(distance.toDouble() > 1000){
            dist = "${"%.2f".format(distance.toDouble() / 1000)} km"
        } else {
            dist = "${(distance.toDouble()).roundToInt()} ${getString(R.string.dist_meter)}"
        }
        return dist
    }
}

