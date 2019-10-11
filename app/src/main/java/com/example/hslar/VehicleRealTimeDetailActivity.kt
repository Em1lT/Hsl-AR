package com.example.hslar

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import com.example.hslar.Model.VehicleInfoDetailModel
import com.example.hslar.Model.VehicleInfoSimpleModel
import com.example.hslar.Model.StopModel
import com.example.hslar.Observer.OnButtonClick
import com.example.hslar.DialogFragment.NotificationDialogFragment
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslpoc.Observer
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_single_bus_detail.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * 07.09.2019
 * Final Activity in the chain. Subsribes to the mqtt service and shows information about the the mobements of the single vehicleInfo. Has a map that can be viewed, you can create notifications
 * and a simple AR. Uses mapBox
 *
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class VehicleRealTimeDetailActivity : AppCompatActivity(), Observer, OnButtonClick, OnMapReadyCallback {

    private lateinit var mqttService: MqttServiceCaller
    private lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService
    private lateinit var httpService: HttpService

    private lateinit var notification: NotificationCompat.Builder
    private lateinit var mapBoxMap: MapboxMap
    private var topic: String = ""
    private lateinit var myLocation: LatLng
    private lateinit var busLocation: LatLng
    private lateinit var vehicleInfo: VehicleInfoSimpleModel
    private lateinit var choosedStop: StopModel
    private val list = mutableListOf<StopModel>()
    private var followBus = true

    private var notificationDetail = false
    private var notifiedDistance = 0
    private var progressMax: Int = 100
    private var notificationSet = false
    private var notificationManager: NotificationManager? = null
    private val FOLLOWERS_CHANNEL = "MAINCHANNEL"

    var nullCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_single_bus_detail)

        vehicleInfo = intent.extras.getSerializable("vehicleInfo") as VehicleInfoSimpleModel
        choosedStop = intent.extras.getSerializable("stop") as StopModel
        endStop.text = intent.extras.getString("EndLine")

        httpService = HttpService()
        locationService = LocationService(this)
        internalStorageService = InternalStorageService()
        getYourLocation()
        var busVeh = ""
        for (i in 0 until (5 - vehicleInfo.veh.length)) {
            busVeh += "0"
        }
        busVeh += vehicleInfo.veh

        topic = "/hfp/v2/journey/ongoing/vp/+/+/$busVeh/${vehicleInfo.route}/+/+/+/+/+/#"
        //busNum.text = vehicleInfo.desi
        mqttService = MqttServiceCaller(this, topic)
        mqttService.registerObserverActivity(this)
        busNum.text = vehicleInfo.desi
        val data = createPostJsonArr(vehicleInfo.desi)
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            list.add(
                StopModel(
                    item.getString("gtfsId"),
                    item.getString("name"),
                    item.getString("lat"),
                    item.getString("lon"),
                    item.getString("zoneId"),
                    item.getString("code"),
                    item.getString("desc"),
                    "0"
                )
            )
        }
        Thread { mqttService.run() }.start()

        mapBox.onCreate(savedInstanceState)
        mapBox.getMapAsync(this)

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val followersChannel = NotificationChannel(
            FOLLOWERS_CHANNEL, "CHANNEL",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        followersChannel.lightColor = Color.GREEN

        notificationManager!!.createNotificationChannel(followersChannel)


        showNavi.setOnClickListener {
            if (navigationMenu.visibility == View.INVISIBLE) {
                navigationMenu.visibility = View.VISIBLE
            } else {
                navigationMenu.visibility = View.INVISIBLE
            }
        }

        bYou.setOnClickListener {
            followBus = false
            startResponseAnimation(bYou)
            moveCameraToLocation(myLocation)
        }
        bStop.setOnClickListener {
            followBus = false
            startResponseAnimation(bStop)
            val latLng = LatLng(choosedStop.lat.toDouble(), choosedStop.lon.toDouble())
            moveCameraToLocation(latLng)
        }
        bBus.setOnClickListener {
            followBus = true
            startResponseAnimation(bBus)
            moveCameraToLocation(busLocation)
        }
        if (vehicleInfo.dist.toInt() > 1000) {
            bNotification.isClickable = false
        }
        bNotification.setOnClickListener {
            if (!notificationSet) {
                val dial = NotificationDialogFragment(vehicleInfo.dist.toInt())
                dial.show(supportFragmentManager, "Notification_popup")
            } else {
                Toast.makeText(this, "Notification already set", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapBoxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->

            val geoJson = GeoJsonOptions().withTolerance(0.4f)
            val symbolManager = SymbolManager(mapBox, mapboxMap, style, null, geoJson)


            style.addImage(
                "HOME_STATION",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_store_black_24dp)
                )!!, true
            )

            style.addImage(
                "OTHER_STATION",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_store_black2_24dp)
                )!!, true
            )


            for (item in list) {

                val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())

                if (item.lat == choosedStop.lat && item.lon == choosedStop.lon) {

                    val symbol6 = SymbolOptions()
                        .withLatLng(latLng)
                        .withIconImage("HOME_STATION")
                        .withIconSize(1.3f)
                        .withDraggable(false)


                    symbolManager.create(symbol6)

                } else {

                    val symbol1 = SymbolOptions()
                        .withLatLng(latLng)
                        .withIconImage("OTHER_STATION")
                        .withIconSize(1.3f)
                        .withDraggable(false)

                    symbolManager.create(symbol1)

                }
            }
            style.addImage(
                "YOU",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_person_black_24dp)
                )!!, true
            )

            style.addImage(
                "BUS_MARKER",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_directions_bus_black_24dp)
                )!!, true
            )

            style.addSource(GeoJsonSource("source-id"))
            style.addLayer(
                SymbolLayer("vehicleInfo", "source-id").withProperties(
                    iconImage("BUS_MARKER"),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true),
                    iconSize(1.3f)
                )
            )

            val latLng1 = LatLng(myLocation.latitude, myLocation.longitude)

            val symbol2 = SymbolOptions()
                .withLatLng(latLng1)
                .withIconImage("YOU")
                .withIconSize(1.3f)
                .withDraggable(false)

            symbolManager.create(symbol2)
        }
        val position = CameraPosition.Builder()
            .target(LatLng(choosedStop.lat.toDouble(), choosedStop.lon.toDouble())).zoom(15.0).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 7000)


    }

    private fun createPostJsonArr(route: String): JSONArray {
        val stopArray: JSONArray
        val json = JSONObject()
        json.put("query", "{routes(name:\"$route\"){ stops {gtfsId name lat lon zoneId code desc}}}")
        val res = httpService.postRequest(json)
        val data = JSONArray(JSONObject(JSONObject(res).getString("data")).getString("routes"))
        val stopNames = JSONObject(data[0].toString()).getString("stops")
        stopArray = JSONArray(stopNames)

        return stopArray
    }

    private fun updateMarkers(vehicleInfo: VehicleInfoDetailModel) {
        if (mapBoxMap.style != null) {
            val busSource = mapBoxMap.style!!.getSource("source-id") as GeoJsonSource
            busSource.setGeoJson(
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(Point.fromLngLat(vehicleInfo.longi.toDouble(), vehicleInfo.lat.toDouble()))
                )
            )
        }
    }

    private fun getYourLocation() {
        val data = internalStorageService.readOnFile(this, "location.txt")
        val lat: Double
        val long: Double
        if (data!!.isNotEmpty()) {
            lat = data.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            myLocation = LatLng(lat, long)

        }
    }

    override fun newMessage(message: JSONObject) {

        if (message.has("VP")) {
            vehiclePosition(message)
        }

        //Vehicle will soon arrive to a stop
        if (message.has("DUE")) {
            arriveSoon(message)
        }
        //Vehicle has arrived to a stop
        if (message.has("ARS")) {
            busOnStop(message)
        }
        //Vehicle passes through a stop without stopping
        if (message.has("PAS")) {

        }
        //Vehicle departs from a stop and leaves the stop radius
        if (message.has("DEP")) {

        }
        //Doors of the vehicle are opened
        if (message.has("DOO")) {

        }
        //Vehicle arrives inside of a stop radius
        if (message.has("ARR")) {

        }
        //Vehicle receives a response to traffic light priority request
        if (message.has("TLA")) {

        }
        //Doors of the vehicle are closed
        if (message.has("DOC")) {

        }
        //Vehicle is ready to depart from a stop
        if (message.has("PDE")) {


        }
    }

    override fun onDialogClickListener(structure: Boolean, notificationDistance: Int) {
        notificationSet = true
        notificationDetail = structure

        createNotificationsWithProgress(notificationDistance)
        if (notificationDetail) {
            createNotificationsWithProgress(notificationDistance)
        } else {
            createNotification()
        }
    }

    private fun createNotification() {

        notification = NotificationCompat.Builder(this, "MAINCHANNEL")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Notification set at stop ${choosedStop.name}")
            .setProgress(progressMax, 0, false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    private fun createNotificationsWithProgress(notificationDistance: Int) {

        notifiedDistance = notificationDistance
        notification = NotificationCompat.Builder(this, FOLLOWERS_CHANNEL)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Notification set at stop ${choosedStop.name}")
            .setProgress(progressMax, 0, false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        notificationManager!!.notify(2, notification.build())

    }

    private fun moveCameraToLocation(latLng: LatLng) {
        val position = CameraPosition.Builder()
            .target(LatLng(latLng.latitude, latLng.longitude)).zoom(15.0).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3500)
    }

    private fun busOnStop(message: JSONObject) {
        val data = JSONObject(message.getString("ARS"))
        val newBus = createBusModel(data, "ARS")
        updateOverley(newBus)
    }

    private fun arriveSoon(message: JSONObject) {
        val data = JSONObject(message.getString("DUE"))
        val newBus = createBusModel(data, "DUE")
        updateOverley(newBus)
    }


    private fun vehiclePosition(message: JSONObject) {
        val data = JSONObject(message.getString("VP"))
        val newBus = createBusModel(data, "VP")
        updateOverley(newBus)
    }

    private fun createBusModel(data: JSONObject, event: String): VehicleInfoDetailModel {
        return VehicleInfoDetailModel(
            data.getString("desi"),
            data.getString("dir"),
            data.getString("oper"),
            data.getString("veh"),
            data.getString("tst"),
            data.getString("tsi"),
            data.getString("spd"),
            data.getString("hdg"),
            data.getString("lat"),
            data.getString("long"),
            data.getString("acc"),
            data.getString("odo"),
            data.getString("drst"),
            data.getString("drst"),
            data.getString("jrn"),
            data.getString("line"),
            data.getString("start"),
            data.getString("loc"),
            data.getString("stop"),
            data.getString("route"),
            data.getString("occu"),
            event
        )
    }

    @SuppressLint("SetTextI18n")
    fun updateOverley(newVehicleInfo: VehicleInfoDetailModel) {

        updateMarkers(newVehicleInfo)
        busLocation = LatLng(newVehicleInfo.lat.toDouble(), newVehicleInfo.longi.toDouble())

        val dis = locationService.calculateDistanceFromTwoPoints(
            choosedStop.lat.toDouble(),
            choosedStop.lon.toDouble(),
            newVehicleInfo.lat.toDouble(),
            newVehicleInfo.longi.toDouble()
        ).roundToInt()

        vehicleInfo.dist = dis.toString()
        checkStation(newVehicleInfo.stop)
        speed.text = (newVehicleInfo.speed.toDouble() * 3.6).roundToInt().toString() + " km/h"
        if (dis > 1000) {
            dist.text = "${("%.2f".format(vehicleInfo.dist.toDouble() / 1000))} km"
        } else {
            dist.text = "${vehicleInfo.dist} m"
        }
        if (newVehicleInfo.drst.toInt() == 0) {
            drs.text = getString(R.string.doorClose)
        } else {
            drs.text = getString(R.string.doorOpen)
        }

        if (followBus) {
            moveCameraToLocation(busLocation)
        }
        if (notificationSet && newVehicleInfo.speed.toDouble() > 5.0) {
            updateNotification()
        }

    }

    @SuppressLint("LogNotTimber")
    private fun updateNotification() {

        if (notificationDetail) {
            Log.d("Main", "here")
            Log.d("Main", (notifiedDistance / vehicleInfo.dist.toDouble() * 100).toString())

            var notDist = vehicleInfo.dist.toDouble() - notifiedDistance
            Log.d("Main", "BUS is ${(100 - ((vehicleInfo.dist.toDouble() - notifiedDistance / notDist) * 100))}% done of the notification")
            if ((notifiedDistance / vehicleInfo.dist.toDouble() * 100) <= 100) {
                notification.setProgress(progressMax, ((notifiedDistance / vehicleInfo.dist.toDouble()) * 100).toInt(), false)
                notificationManager!!.notify(2, notification.build())
            } else {
                notification.setContentTitle("Bus is now $notifiedDistance meters from the station")
                notification.setOngoing(false)
                notificationManager!!.notify(2, notification.build())
                notificationSet = false
            }
        } else {
            if (vehicleInfo.dist.toDouble() < notifiedDistance.toDouble()) {
                notification.setContentTitle("Bus is now $notifiedDistance meters from the station")
                notificationManager!!.notify(2, notification.build())
                notificationSet = false
            }
        }


    }

    private fun checkStation(stopNum: String) {

        if (nullCount % 10 == 0) {
            if (stopNum != "null") {

                val json = JSONObject()
                json.put("query", "{stop(id: \"HSL:$stopNum\"){name lat lon}}")
                val res = httpService.postRequest(json)
                val data = JSONObject(JSONObject(res).getString("data")).getString("stop")

                stop.text = JSONObject(data).getString("name")
                nullCount = 1

            } else {
                stop.text = ""
                nullCount = 1
            }
        } else {
            nullCount++
        }
    }

    override fun onDestroy() {
        mqttService.unsubscribe(topic)
        mqttService.deRegisterObserverActivity(this)
        super.onDestroy()
        mapBox.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mapBox.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapBox.onResume()
    }


    override fun onStop() {
        super.onStop()
        mapBox.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapBox.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapBox.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        mapBox.onPause()
    }

    fun startResponseAnimation(button: ImageButton) {
        button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_response))
    }
}