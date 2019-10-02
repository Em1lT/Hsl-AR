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
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import com.example.hslar.Adapter.BusListAdapter
import com.example.hslar.Model.BusDetailModel
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel
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
class BusListFragment(val routeModel: RouteModel, val stopModel: StopModel) : Fragment(), Observer {

    lateinit var adapter: BusListAdapter
    lateinit var mqttService: MqttServiceCaller
    lateinit var internalStorageService: InternalStorageService
    lateinit var locationService: LocationService

    private var topic = "/hfp/v2/journey/ongoing/vp/+/+/+/${routeModel.gtfsId.substringAfter(":")}/1/+/+/+/+/#"
    private var topic1 = "/hfp/v2/journey/ongoing/vp/+/+/+/${routeModel.gtfsId.substringAfter(":")}/2/+/+/+/+/#"
    private var list = mutableListOf<BusSimpleModel>()

    //TODO: create a list for both directions & display the line somehow
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mqttService = MqttServiceCaller(this.requireContext(), topic)
        mqttService.registerObserverFragment(this)
        internalStorageService = InternalStorageService()
        locationService = LocationService(activity!!.applicationContext)


        val view = inflater.inflate(R.layout.fragment_bus_list, container, false)
        unSubsribeWithDelay()
        Thread { mqttService.run() }.start()

        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, list)
        view.bussesList.adapter = adapter

        view.sortByDescending.setOnClickListener {
            startResponseAnimation(view.sortByDescending)
            sortDescending()
        }
        view.sortByClosest.isEnabled = false
        view.bussesList.isEnabled = false
        view.sortByDescending.isEnabled = false
        if(progressBar1 != null) {
            progressBar1.visibility = View.VISIBLE
        }


            view.sortByClosest.setOnClickListener {
            startResponseAnimation(view.sortByClosest)
            sortByDistance()
        }
        view.bussesList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, SingleBusDetailActivity::class.java).apply {
                putExtra("bus", adapter.getItem(i))
                putExtra("stop", stopModel)
            }
            mqttService.deRegisterObserverFragment(this)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view
    }
    fun  startResponseAnimation(but: Button){
        but.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_response))

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
    private fun unsubsribe(){
        mqttService.unsubscribe(topic)
        //TODO: mqttService disconnect from the client(When trying to disconnect it crashes)
        //mqttService.disconnect()
    }
    private fun unSubsribeWithDelay(){
        Thread(Runnable {
            Timer().schedule(timerTask {
                unsubsribe()

                calcDistanceForAll()
            }, 2500)
        }).start()
    }
    private fun reSubsribeWithDelay(){
        Thread(Runnable {
            Timer().schedule(timerTask {
                unsubsribe()

                calcDistanceForAll()
            }, 2500)
        }).start()
    }
    fun sortByDistance(){

        var sortedList = list.sortedWith(compareBy { it.dist.toDouble()})
        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter

    }

    fun sortDescending(){
        var sortedList = list.sortedWith(compareBy { it.veh})

        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter
    }

    fun calcDistanceForAll(){
        for (item in list) {

            if(item.lat.toDouble() != null){
                var dist = locationService.calculateDistanceFromTwoPoints(
                    stopModel.lat.toDouble(),
                    stopModel.lon.toDouble(),
                    item.lat.toDouble(),
                    item.longi.toDouble()
                )
                item.dist = dist.toInt().toString()
            }

        }
        var sortedList = list.sortedWith(compareBy({ it.dist.toDouble()}))


        activity!!.runOnUiThread {
            progressBar1.visibility = View.INVISIBLE
            view!!.sortByClosest.isEnabled = true
            view!!.bussesList.isEnabled = true
            view!!.sortByDescending.isEnabled = true
            adapter.notifyDataSetChanged()
        }
    }
}
