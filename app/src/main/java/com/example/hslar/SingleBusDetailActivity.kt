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
import com.example.hslar.Model.BusDetailModel
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.StopModel
import com.example.hslar.Observer.OnButtonClick
import com.example.hslar.PopUp.NotificationPopUp
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
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
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

class SingleBusDetailActivity : AppCompatActivity(), Observer, OnButtonClick, OnMapReadyCallback {

    private lateinit var mqttService: MqttServiceCaller
    private lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService
    private lateinit var httpService: HttpService

    // private lateinit var mapFragment: SupportMapFragment
    private lateinit var notification: NotificationCompat.Builder
    private lateinit var mapBoxMap: MapboxMap
    private var topic: String = ""
    private lateinit var myLocation: LatLng
    private lateinit var busLocation: LatLng
    private lateinit var bus: BusSimpleModel
    private lateinit var choosedStop: StopModel
    private lateinit var busSymbol: Symbol
    private val list = mutableListOf<StopModel>()
    private var followBus = true

    private var progressMax: Int = 0
    private var notificationSet = false
    private var notificationManager: NotificationManager? = null
    val FOLLOWERS_CHANNEL = "MAINCHANNEL"

    var nullCount = 1

    //TODO: MORE INFO FROM STATINONS & AR & NOTIFICATION?
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.access_token))
        setContentView(R.layout.activity_single_bus_detail)

        bus = intent.extras.getSerializable("bus") as BusSimpleModel
        choosedStop = intent.extras.getSerializable("stop") as StopModel
        endStop.text = intent.extras.getString("EndLine")

        httpService = HttpService()
        locationService = LocationService(this)
        internalStorageService = InternalStorageService()
        getYourLocation()
        var busVeh = ""
        for(i in 0 until (5 - bus.veh.length)){
            busVeh += "0"
        }
        busVeh += bus.veh

        topic = "/hfp/v2/journey/ongoing/vp/+/+/$busVeh/${bus.route}/+/+/+/+/+/#"
        //busNum.text = bus.desi
        mqttService = MqttServiceCaller(this, topic)
        mqttService.registerObserverActivity(this)
        busNum.text = bus.desi
        var data = createPostJsonArr(bus.desi)
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
                Context.NOTIFICATION_SERVICE) as NotificationManager

        val followersChannel = NotificationChannel(
            FOLLOWERS_CHANNEL, "CHANNEL",
            NotificationManager.IMPORTANCE_DEFAULT)

        followersChannel.lightColor = Color.GREEN

        notificationManager!!.createNotificationChannel(followersChannel)


        showNavi.setOnClickListener {
            if(navigationMenu.visibility == View.INVISIBLE){
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
        if(bus.dist.toInt() > 1000){
            bNotification.isClickable = false
        }
        bNotification.setOnClickListener {
                val dial = NotificationPopUp(bus.dist.toInt())
                dial.show(supportFragmentManager,"Notification_popup")
        }
        bAr.setOnClickListener {
            //TODO: AR ELEMENT, could be made with Unity and combine to activity??
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
      mapBoxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {style ->

            val geoJson = GeoJsonOptions().withTolerance(0.4f)
            val symbolManager = SymbolManager(mapBox, mapboxMap, style, null, geoJson)


            style.addImage(
                "HOME_STATION",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_store_black_24dp))!!, true)

            style.addImage(
                "OTHER_STATION",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_store_black2_24dp))!!, true)


            for(item in list){

                val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())

                if(item.lat == choosedStop.lat && item.lon == choosedStop.lon) {

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
                    ContextCompat.getDrawable(this, R.drawable.ic_person_black_24dp))!!, true)

            style.addImage(
                "BUS_MARKER",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_directions_bus_black_24dp))!!, true)

            style.addSource(GeoJsonSource("source-id"))
            style.addLayer(SymbolLayer("bus","source-id").withProperties(
                iconImage("BUS_MARKER"),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconSize(1.3f)
            ))

            val latLng1 = LatLng(myLocation.latitude, myLocation.longitude)

            var symbol2 = SymbolOptions()
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
    fun createPostJsonArr(route: String): JSONArray {
        var stopArray: JSONArray
        val json = JSONObject()
        json.put("query", "{routes(name:\"${route}\"){ stops {gtfsId name lat lon zoneId code desc}}}")
        val res = httpService.postRequest(json)
        val data = JSONArray(JSONObject(JSONObject(res).getString("data")).getString("routes"))
        val stopNames = JSONObject(data[0].toString()).getString("stops")
        stopArray = JSONArray(stopNames)

        return stopArray
    }

    fun updateMarkers(bus: BusDetailModel) {
        val latLng = LatLng(bus.lat.toDouble(), bus.longi.toDouble())
        if (mapBoxMap.style != null) {
            val busSource = mapBoxMap.style!!.getSource("source-id") as GeoJsonSource
            busSource.setGeoJson(
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(Point.fromLngLat(bus.longi.toDouble(), bus.lat.toDouble()))
                )
            )
        }
    }
    private fun getYourLocation(){
        val data = internalStorageService.readOnFile(this,"location.txt")
        val lat: Double
        val long: Double
        if(data!!.isNotEmpty()) {
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
            BusOnStop(message)
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
        Log.d("Main", "Start Notification with ${structure} distance between $notificationDistance")
        createNotificationsWithProgress(notificationDistance)
        /*if(structure){
            createNotificationsWithProgress(notificationDistance)
        } else {
            createNotification(notificationDistance)
        }*/
    }
    private fun createNotification(notificationDistance: Int) {

        notification = NotificationCompat.Builder(this, "MAINCHANNEL")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Bus at stop ${choosedStop.name}")
            .setProgress(progressMax,0 ,false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

    }
    private fun createNotificationsWithProgress(notificationDistance: Int) {
        progressMax = notificationDistance

        notification = NotificationCompat.Builder(this, FOLLOWERS_CHANNEL)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Bus at stop ${choosedStop.name}")
            .setProgress(progressMax,0 ,false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        notificationManager!!.notify(2, notification.build())

    }

    private fun moveCameraToLocation(latLng: LatLng){
        var position = CameraPosition.Builder()
            .target(LatLng(latLng.latitude, latLng.longitude)).zoom(15.0).build()

        mapBoxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3500)
    }

    private fun BusOnStop(message: JSONObject) {
        val data = JSONObject(message.getString("ARS"))
        val newBus = createBusModel(data, "ARS")
        updateOverley(newBus)
    }

    private fun arriveSoon(message: JSONObject) {
        val data = JSONObject(message.getString("DUE"))
        val newBus = createBusModel(data, "DUE")
        updateOverley(newBus)
    }


    fun vehiclePosition(message: JSONObject){
        val data = JSONObject(message.getString("VP"))
        val newBus = createBusModel(data, "VP")
        updateOverley(newBus)
    }

    fun createBusModel(data: JSONObject, event: String): BusDetailModel {
        val busDetail = BusDetailModel(
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
        return busDetail
    }
    @SuppressLint("SetTextI18n")
    fun updateOverley(newBus: BusDetailModel){

        updateMarkers(newBus)
        busLocation = LatLng(newBus.lat.toDouble(), newBus.longi.toDouble())

        val dis = locationService.calculateDistanceFromTwoPoints(choosedStop.lat.toDouble(), choosedStop.lon.toDouble(), newBus.lat.toDouble(), newBus.longi.toDouble()).roundToInt()

        bus.dist = dis.toString()
        checkStation(newBus.stop)
        speed.text = (newBus.speed.toDouble() * 3.6).roundToInt().toString() + " km/h"
        lat.text = newBus.lat
        longi.text = newBus.longi
        odo.text = newBus.odo
        if(dis > 1000){
            dist.text = "${("%.2f".format(bus.dist.toDouble() / 1000))} km"
        } else {
            dist.text = "${bus.dist} m"
        }
        if(newBus.drst.toInt() == 0){
            drs.text = getString(R.string.doorClose)
        } else{
            drs.text = getString(R.string.doorOpen)
        }
        event.text = newBus.event
        hdg.text = newBus.hdg

        if(followBus){
            moveCameraToLocation(busLocation)
        }
        if(notificationSet){
            updateNotification()
        }

    }
    private fun updateNotification(){

        if(bus.dist.toInt() > 10){
            notification.setProgress(progressMax, ((progressMax / bus.dist.toInt()) * 100), false)
            notificationManager!!.notify(2, notification.build())
        } else {
            notification.setContentText("Bus is now $progressMax from the station")
            notificationManager!!.notify(2, notification.build())
        }

    }

    private fun checkStation(stopNum: String) {

        if(nullCount % 10 == 0){
            if(stopNum != "null") {

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
        }else {
            nullCount++
        }
    }

    override fun onDestroy() {
        mqttService.unsubscribe(topic)
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
    fun startResponseAnimation(button: ImageButton){
        button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_response))
    }
}