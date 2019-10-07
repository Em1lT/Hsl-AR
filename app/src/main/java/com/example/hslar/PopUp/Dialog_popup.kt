package com.example.hslar.PopUp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
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
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.pop_up.*
import kotlinx.android.synthetic.main.pop_up.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 07.09.2019
 * DialogFragment that displays a map to the choosed destination
 *
 */

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ValidFragment")
class DialogPopup(val location: Location, val location1: Location) : DialogFragment(), OnMapReadyCallback {

    private var cameraOnYou = true
    private var walkingOrNot = true

    lateinit var route: DirectionsRoute

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {

            val from = Point.fromLngLat(location.longitude, location.latitude)
            val to = Point.fromLngLat(location1.longitude, location1.latitude)

            val latLng = LatLng(location.latitude, location.longitude)
            val latLng1 = LatLng(location1.latitude, location1.longitude)

            val geoJson = GeoJsonOptions().withTolerance(0.4f)
            val symbolManager = SymbolManager(mapView1, mapboxMap, it, null, geoJson)

            it.addImage(
                "ID_YOU",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(
                        activity!!.applicationContext,
                        R.drawable.ic_person_black_24dp
                    )
                )!!,
                true
            )

            it.addImage(
                "ID_BUS",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(
                        activity!!.applicationContext,
                        R.drawable.ic_store_black_24dp
                    )
                )!!,
                true
            )

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

            getRouteWalking(from, to)
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

        view.showNavi.setOnClickListener {
            if (view.navigationMenu1.visibility == View.INVISIBLE) {
                view.navigationMenu1.visibility = View.VISIBLE
            } else {
                view.navigationMenu1.visibility = View.INVISIBLE
            }
        }
        view.walkOrDrive.setOnClickListener {
            startResponseAnimation(view.walkOrDrive)
        }
        view.you.setOnClickListener {

            startResponseAnimation(view.you)
            val position: CameraPosition = if (cameraOnYou) {
                view.you.setImageResource(R.drawable.ic_person_black_24dp)
                cameraOnYou = false
                CameraPosition.Builder()
                    .target(LatLng(location1.latitude, location1.longitude)).zoom(15.0).build()
            } else {
                view.you.setImageResource(R.drawable.ic_store_black_24dp)
                cameraOnYou = true
                CameraPosition.Builder()
                    .target(LatLng(location.latitude, location.longitude)).zoom(15.0).build()

            }
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)
        }
        view.route.setOnClickListener {
            val navigationLauncherOptions = NavigationLauncherOptions.builder() //1
                .directionsRoute(route) //2
                .shouldSimulateRoute(true) //3
                .build()

            NavigationLauncher.startNavigation(activity, navigationLauncherOptions) //4
        }
        view.mapView1.onCreate(savedInstanceState)
        view.mapView1.getMapAsync(this)
    }

    private fun startResponseAnimation(button: ImageButton) {
        button.startAnimation(AnimationUtils.loadAnimation(activity!!.applicationContext, R.anim.button_response))
    }

    private fun getRouteWalking(origin: Point, dest: Point) {
        Thread(Runnable {

            NavigationRoute.builder(activity!!.applicationContext)
                .accessToken(Mapbox.getAccessToken()!!)
                .origin(origin)
                .destination(dest)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .build()
                .getRoute(object : Callback<DirectionsResponse> { //6
                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

                    }

                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        if (response.body() == null) {
                            return
                        } else if (response.body()!!.routes().size == 0) {
                            return
                        }

                        route = response.body()!!.routes()[0] as DirectionsRoute

                        if (navigationMapRoute != null) {
                            return
                        } else {
                            navigationMapRoute = NavigationMapRoute(null, mapView1, map)
                        }
                        navigationMapRoute!!.addRoute(route)
                    }
                })
        }).start()
    }

    fun getRouteDriving(origin: Point, dest: Point) {
        Thread(Runnable {

            NavigationRoute.builder(activity!!.applicationContext)
                .accessToken(Mapbox.getAccessToken()!!)
                .origin(origin)
                .destination(dest)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .build()
                .getRoute(object : Callback<DirectionsResponse> { //6
                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

                    }

                    override fun onResponse(
                        call: Call<DirectionsResponse>,
                        response: Response<DirectionsResponse>
                    ) {
                        if (response.body() == null) {
                            return
                        } else if (response.body()!!.routes().size == 0) {
                            return
                        }

                        route = response.body()!!.routes()[0] as DirectionsRoute

                        if (navigationMapRoute != null) {
                            return
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
              //view!!.mapView1.onDestroy()
          }*/
        super.dismiss()
    }

}
/*override fun onDestroyView() {
        super.onDestroyView()
        /*if(view!!.mapView1 != null){
            view!!.mapView1.onDestroy()
        }*/