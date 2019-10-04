package com.example.hslar.PopUp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.R
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
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
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.pop_up.*
import kotlinx.android.synthetic.main.pop_up.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("ValidFragment")
class Dialog_popup(val location: Location, val location1: Location) : DialogFragment(), OnMapReadyCallback {

    var cameraOnYou = true

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS){

            val from = Point.fromLngLat(location.longitude, location.latitude)
            val to = Point.fromLngLat(location1.longitude, location1.latitude)

            val latLng = LatLng(location.latitude, location.longitude)
            val latLng1 = LatLng(location1.latitude, location1.longitude)

            val geoJson = GeoJsonOptions().withTolerance(0.4f)
            val symbolManager = SymbolManager(mapView1, mapboxMap, it, null, geoJson)

            it.addImage("ID_YOU",
                BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(activity!!.applicationContext, R.drawable.ic_person_black_24dp))!!,
                true)

            it.addImage("ID_BUS",
                BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(activity!!.applicationContext, R.drawable.ic_store_black_24dp))!!,
                true)

            val symbol = SymbolOptions()
                .withLatLng(latLng)
                .withIconImage("ID_YOU")
                .withIconSize(2.0f)
                .withDraggable(false)
                .withTextField(getString(R.string.you))

            symbolManager.create(symbol)


            val symbol1 = SymbolOptions()
                .withLatLng(latLng1)
                .withIconImage("ID_BUS")
                .withIconSize(2.0f)
                .withDraggable(false)
                .withTextField(getString(R.string.stop))
            symbolManager.create(symbol1)

            val position = CameraPosition.Builder()
                .target(LatLng(location.latitude, location.longitude)).zoom(15.0).build()

            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)

            getRoute(from, to)
        }

    }
    var navigationMapRoute: NavigationMapRoute? = null
    lateinit var map: MapboxMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.access_token))

        return inflater.inflate(R.layout.pop_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.you.setOnClickListener {

            val position: CameraPosition = if(cameraOnYou){
                cameraOnYou = false
                CameraPosition.Builder()
                    .target(LatLng(location1.latitude, location1.longitude)).zoom(15.0).build()
            } else {
                cameraOnYou = true
                CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude)).zoom(15.0).build()

            }
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)
        }
        view.mapView1.onCreate(savedInstanceState)
        view.mapView1.getMapAsync(this)
    }
    fun getRoute(origin: Point, dest: Point){
        Thread(Runnable{

        NavigationRoute.builder(activity!!.applicationContext)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(origin)
            .destination(dest)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .build()
            .getRoute(object : Callback<DirectionsResponse> { //6
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

                }
                override fun onResponse(call: Call<DirectionsResponse>,
                                        response: Response<DirectionsResponse>) {
                    if(response.body() == null){
                        Log.d("Main", "error, body is null")
                        return
                    } else if(response.body()!!.routes().size == 0) {
                        Log.d("", "no routes found")
                        return
                    }

                    var route = response.body()!!.routes()[0] as DirectionsRoute
                    Log.d("Main", route.toString())

                    if(navigationMapRoute != null){
                        navigationMapRoute!!.removeRoute()
                    } else {
                        navigationMapRoute = NavigationMapRoute(null, mapView1, map)
                    }
                    navigationMapRoute!!.addRoute(route)
                }
            })
        }).start()
    }
    override fun onStart() {
        super.onStart()
        view!!.mapView1.onStart()
    }

    override fun onResume() {
        super.onResume()
        view!!.mapView1.onResume()
    }

    override fun onPause() {
        super.onPause()
        view!!.mapView1.onPause()
    }

    override fun onStop() {
        super.onStop()
        view!!.mapView1.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        view!!.mapView1.onLowMemory()
    }

    override fun onDismiss(dialog: DialogInterface?) {
      /*  if (view!!.mapView1 != null) {
            view!!.mapView1.onPause()
            view!!.mapView1.onStop()
            //TODO: mapBox freezes on with onDestroy method?? check https://github.com/mapbox/mapbox-gl-native/blob/ba1d5b8b41cc7082cde60d5f3fa4b4d36427ff2a/platform/android/MapboxGLAndroidSDKTestApp/src/main/java/com/mapbox/mapboxsdk/testapp/activity/maplayout/MapInDialogActivity.java#L73
            //view!!.mapView1.onDestroy()
        }*/
        super.dismiss()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        /*if(view!!.mapView1 != null){
            view!!.mapView1.onDestroy()
        }*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view!!.mapView1.onSaveInstanceState(outState)
    }

    /*
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
    }*/
}