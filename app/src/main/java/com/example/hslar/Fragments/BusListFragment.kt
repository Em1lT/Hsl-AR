package com.example.hslar.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import com.example.hslar.Adapter.BusListAdapter
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel
import com.example.hslar.R
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslar.SingleBusDetailActivity
import com.example.hslpoc.Observer
import kotlinx.android.synthetic.main.fragment_bus_list.*
import kotlinx.android.synthetic.main.fragment_bus_list.view.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

@SuppressLint("ValidFragment")
class BusListFragment(private val routeModel: RouteModel, private val stopModel: StopModel) : Fragment(), Observer {

    lateinit var adapter: BusListAdapter
    lateinit var adapter1: BusListAdapter
    lateinit var mqttService: MqttServiceCaller
    lateinit var internalStorageService: InternalStorageService
    lateinit var locationService: LocationService

    private var topic = "/hfp/v2/journey/ongoing/vp/+/+/+/${routeModel.gtfsId.substringAfter(":")}/+/+/+/+/+/#"
    private var listDirection = mutableListOf<BusSimpleModel>()
    private var listOtherDirection = mutableListOf<BusSimpleModel>()

    //TODO: create a list for both directions & display the line somehow
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mqttService = MqttServiceCaller(this.requireContext(), topic)
        mqttService.registerObserverFragment(this)
        internalStorageService = InternalStorageService()
        locationService = LocationService(activity!!.applicationContext)

        val firstEndingStop = routeModel.longName.substringBefore("-")
        val secondEndingStop = routeModel.longName.substringAfterLast("-")

        val view = inflater.inflate(R.layout.fragment_bus_list, container, false)

        unSubsribeWithDelay()
        Thread { mqttService.run() }.start()

        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, listDirection)
        view.bussesList.adapter = adapter

        adapter1 = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, listOtherDirection)
        view.bussesListOther.adapter = adapter1

        view.endingLine.text = secondEndingStop
        view.endingLineSecond.text = firstEndingStop

        view.sortByDescending.setOnClickListener {
            startResponseAnimation(view.sortByDescending)
            sortDescending()
        }
        view.sortByClosest.isEnabled = false
        view.bussesList.isEnabled = false
        view.bussesListOther.isEnabled = false
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
        view.bussesListOther.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, SingleBusDetailActivity::class.java).apply {
                putExtra("bus", adapter1.getItem(i))
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

        if (message.has("VP")) {
            var data = JSONObject(message.getString("VP"))
            var dir = data.getString("dir")
            var newBus = BusSimpleModel(
                data.getString("veh"),
                data.getString("route"),
                data.getString("desi"),
                data.getString("lat"),
                data.getString("long"),
                "0"
            )

            if(dir == "1"){
            if (listDirection.size < 1) {
                listDirection.add(newBus)

            }
            for ((i, item) in listDirection.withIndex()) {
                if (item.veh == data.getString(("veh"))) {
                    listDirection[i] = newBus
                    return
                }
            }
                listDirection.add(newBus)
        } else {
                if (listOtherDirection.size < 1) {
                    listOtherDirection.add(newBus)

                }
                for ((i, item) in listOtherDirection.withIndex()) {
                    if (item.veh == data.getString(("veh"))) {
                        listOtherDirection[i] = newBus
                        return
                    }
                }
                listOtherDirection.add(newBus)
            }
        }
        adapter.notifyDataSetChanged()
        adapter1.notifyDataSetChanged()

    }
    private fun unsubsribe(){
        Log.d("Main", "unsubscribe")
        mqttService.unsubscribe(topic)
        //TODO: mqttService disconnect from the client(When trying to disconnect it crashes)
        //mqttService.disconnect()
    }
    private fun unSubsribeWithDelay(){
        Thread(Runnable {
            Timer().schedule(timerTask {
                unsubsribe()

                calcDistanceForAll()
            }, 6000)
        }).start()
    }
    fun sortByDistance(){

        var sortedList = listDirection.sortedWith(compareBy { it.dist.toDouble()})
        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter

        var sortedList1 = listOtherDirection.sortedWith(compareBy { it.dist.toDouble()})
        adapter1 = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList1)
        view!!.bussesListOther.adapter = adapter1

    }

    fun sortDescending(){
        var sortedList = listDirection.sortedWith(compareBy { it.veh})

        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter

        var sortedList1= listOtherDirection.sortedWith(compareBy { it.veh})

        adapter1 = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList1)
        view!!.bussesListOther.adapter = adapter1
    }

    fun calcDistanceForAll(){

        for (item in listDirection) {

            Log.d("Main", "${item.lat}::::::::::::: ${item.longi}")
            if(item.lat != "null" && item.longi != "null"){
                var dist = locationService.calculateDistanceFromTwoPoints(
                    stopModel.lat.toDouble(),
                    stopModel.lon.toDouble(),
                    item.lat.toDouble(),
                    item.longi.toDouble()
                )
                item.dist = dist.toInt().toString()
            } else {
                item.dist = "0"
            }
        }
        for (item in listOtherDirection) {

            Log.d("Main", "${item.lat}::::::::::::: ${item.longi}")
            if(item.lat != "null" && item.longi != "null"){
                var dist = locationService.calculateDistanceFromTwoPoints(
                    stopModel.lat.toDouble(),
                    stopModel.lon.toDouble(),
                    item.lat.toDouble(),
                    item.longi.toDouble()
                )
                item.dist = dist.toInt().toString()
            } else {
                item.dist = "0"
            }
        }

        activity!!.runOnUiThread {
            progressBar1.visibility = View.INVISIBLE
            view!!.sortByClosest.isEnabled = true
            view!!.bussesList.isEnabled = true
            view!!.bussesListOther.isEnabled = true
            view!!.sortByDescending.isEnabled = true
            adapter.notifyDataSetChanged()
            adapter1.notifyDataSetChanged()

        }
    }
}
