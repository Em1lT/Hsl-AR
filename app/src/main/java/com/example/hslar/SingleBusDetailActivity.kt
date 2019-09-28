package com.example.hslar

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.hslar.Model.BusDetailModel
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.StopModel
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslpoc.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.android.synthetic.main.activity_single_bus_detail.*
import org.json.JSONObject
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import kotlin.math.roundToInt


class SingleBusDetailActivity : AppCompatActivity(), Observer, OnMapReadyCallback {

    var topic: String = ""
    private lateinit var mqttService: MqttServiceCaller
    lateinit var httpService: HttpService
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var bus: BusSimpleModel
    private lateinit var busMarker: Marker

    var nullCount = 1
    val list = mutableListOf<StopModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_bus_detail)

        bus = intent.extras.getSerializable("bus") as BusSimpleModel

        httpService = HttpService()
        var busVeh = ""
        for(i in 0 until (5 - bus.veh.length)){
            busVeh += "0"
        }
        busVeh += bus.veh

        topic = "/hfp/v2/journey/ongoing/+/+/+/$busVeh/${bus.route}/+/+/+/+/+/#"
        Log.d("Main", topic)
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

       mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        bAr.setOnClickListener {
            //TODO: AR ELEMENT, could be made with Unity and combine to activity??
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map


        //TODO: Create custom googleMaps markers for the bus & the stop
        //TODO: Decide to use google maps or open streetmap???
        //TODO: Check your own location
        //TODO: route that goes through of the bus stops(check google maps docs)

        for(item in list){
            val latLng = LatLng(item.lat.toDouble(), item.lon.toDouble())
            googleMap.addMarker(MarkerOptions().position(latLng).title(item.name)
                .snippet("Zone: ${item.zoneId} desc: ${item.desc}")
                .icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

        }
        val latLng = LatLng(bus.lat.toDouble(), bus.longi.toDouble())
        busMarker = googleMap.addMarker(MarkerOptions()
            .position(latLng)
            .title(bus.veh)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
    }
    fun createPostJsonArr(route: String): JSONArray{
        var stopArray: JSONArray = JSONArray()
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
        googleMap.let {
            busMarker.position =  latLng
        }
    }

    override fun newMessage(message: JSONObject) {

        //TODO: Create something for every event_type. Check https://digitransit.fi/en/developers/apis/4-realtime-api/vehicle-positions-2/
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

    private fun BusOnStop(message: JSONObject) {
        var data = JSONObject(message.getString("ARS"))
        var newBus = createBusModel(data, "ARS")
        updateOverley(newBus)
    }

    private fun arriveSoon(message: JSONObject) {
        var data = JSONObject(message.getString("DUE"))
        var newBus = createBusModel(data, "DUE")
        updateOverley(newBus)
    }


    fun vehiclePosition(message: JSONObject){
        var data = JSONObject(message.getString("VP"))
        var newBus = createBusModel(data, "VP")
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
        checkStation(newBus.stop)
        //TODO: binding

        speed.text = (newBus.speed.toDouble() * 3.6).roundToInt().toString() + " km/h"
        lat.text = newBus.lat
        longi.text = newBus.longi
        odo.text = newBus.odo
        if(newBus.drst.toInt() == 0){
            drs.text = "ovet kiinni"
        } else{
            drs.text = "ovet auki"
        }
        event.text = newBus.event
        hdg.text = newBus.hdg

    }
    private fun checkStation(stopNum: String) {

        if(nullCount % 10 == 0){
            if(stopNum != "null") {
                Log.d("Main", "get stop")

                var json = JSONObject()
                json.put("query", "{stop(id: \"HSL:$stopNum\"){name lat lon}}")
                val res = httpService.postRequest(json)
                var data = JSONObject(JSONObject(res).getString("data")).getString("stop")

                Log.d("Main", JSONObject(data).getString("name"))
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
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mqttService.unsubscribe(topic)
    }
}
