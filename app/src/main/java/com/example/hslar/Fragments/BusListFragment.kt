package com.example.hslar.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.Adapter.BusListAdapter
import com.example.hslar.Model.BusDetailModel
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.RouteModel
import com.example.hslar.R
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.example.hslar.SingleBusDetailActivity
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslpoc.Observer
import kotlinx.android.synthetic.main.fragment_bus_list.*
import kotlinx.android.synthetic.main.fragment_bus_list.view.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

@SuppressLint("ValidFragment")
class BusListFragment(val routeModel: RouteModel) : Fragment(), Observer {

    lateinit var adapter: BusListAdapter
    lateinit var mqttService: MqttServiceCaller
    lateinit var internalStorageService: InternalStorageService
    lateinit var locationService: LocationService

    private var topic = "/hfp/v2/journey/ongoing/vp/+/+/+/${routeModel.gtfsId.substringAfter(":")}/+/+/+/+/+/#"
    private var list = mutableListOf<BusSimpleModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("Main", topic)

        mqttService = MqttServiceCaller(this.requireContext(), topic)
        mqttService.registerObserverFragment(this)
        internalStorageService = InternalStorageService()

        val view = inflater.inflate(R.layout.fragment_bus_list, container, false)
        unSubsribeWithDelay()
        Thread { mqttService.run() }.start()

        adapter = BusListAdapter(this.requireContext(), R.layout.busline_list, list)
        view.bussesList.adapter = adapter

        view.sortByDescending.setOnClickListener {
            sortDescending()
        }
        view.sortByClosest.setOnClickListener {
            calcDistanceForAll()
        }
        view.bussesList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, SingleBusDetailActivity::class.java).apply {
                putExtra("bus", list[i])
            }
            mqttService.deRegisterObserverFragment(this)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
    }
    override fun newMessage(message: JSONObject) {

        //TODO: CREATE A PLEASENT LOADING SCREEN WHEN MQTT DATA IS RECEIVED
        if (message.has("VP")) {
            var data = JSONObject(message.getString("VP"))
            var newBus = BusSimpleModel(
                data.getString("veh"),
                data.getString("route"),
                data.getString("desi"),
                data.getString("lat"),
                data.getString("long"),
                "0"
            )
            if (list.size < 1) {
                list.add(newBus)
            }
            for ((i, item) in list.withIndex()) {
                if (item.veh == data.getString(("veh"))) {
                    list[i] = newBus
                    return
                }
            }
            list.add(newBus)
        }
        adapter.notifyDataSetChanged()
    }
    fun unsubsribe(){
        Log.d("Main", "stop")
        mqttService.unsubscribe(topic)
        //TODO: mqttService disconnect from the client(When trying to disconnect it crashes)
        //mqttService.disconnect()
    }
    fun unSubsribeWithDelay(){
        Thread(Runnable {
            Timer().schedule(timerTask {
                unsubsribe()
            }, 10000)
        }).start()
    }
    fun sortDescending(){
        var sortedList = list.sortedWith(compareBy({ it.veh}))
        adapter = BusListAdapter(this.requireContext(), R.layout.busline_list, sortedList)
        view!!.bussesList.adapter = adapter
    }
    fun calcDistanceForAll(){

        var data = internalStorageService.readOnFile(activity!!.applicationContext,"location.txt")
        locationService = LocationService(activity!!.applicationContext)

        if(data!!.isNotEmpty()){
            var lat = data!!.substringBefore(":").toDouble()
            var long = data.substringAfter(":").toDouble()

            for (item in adapter.items){
                var dist = locationService.calculateDistance(lat, long, item.lat.toDouble(), item.longi.toDouble())
                item.dist = dist.toString()
            }

            var sortedList = list.sortedWith(compareBy({ it.dist}))
            for(item in sortedList){
                Log.d("Main", item.dist)
            }
            adapter = BusListAdapter(this.requireContext(), R.layout.busline_list, sortedList)
            view!!.bussesList.adapter = adapter

        } else {
            Log.d("Main", "no location available")
        }
    }
}
