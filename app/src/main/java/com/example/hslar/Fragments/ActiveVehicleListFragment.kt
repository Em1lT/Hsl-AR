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
import com.example.hslar.DialogFragment.VehicleStopsDialogFragment
import com.example.hslar.Model.VehicleInfoSimpleModel
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel
import com.example.hslar.R
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslar.VehicleRealTimeDetailActivity
import com.example.hslpoc.Observer
import kotlinx.android.synthetic.main.fragment_bus_list.*
import kotlinx.android.synthetic.main.fragment_bus_list.view.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

/**
 * 07.09.2019
 *Fragment that gets all of the ongoing vehicles on the choosed route. We do this by using an mqtt message broker that hsl provides and create a list from the vehicles that send us message.
 * The message broker is open about 6 seconds before we unsubscribe from the message broker. Then we calculate the distance from all of the busses on the list and display the information in a custom listView
 * We use a Observer interface that we register in the mqttService class.
 * There are 2 list. one for every direction. That way it's easier to understand which way you want to go. End stop is also displayed.
 * There is a filter for changing the order to descending or for the closest bus from the STOP you chose earlier.
 *
 */

@SuppressLint("ValidFragment")
class ActiveVehicleListFragment(private val routeModel: RouteModel, private val stopModel: StopModel) : Fragment(),
    Observer {

    private lateinit var adapter: BusListAdapter
    private lateinit var adapter1: BusListAdapter
    private lateinit var mqttService: MqttServiceCaller
    private lateinit var internalStorageService: InternalStorageService
    private lateinit var locationService: LocationService

    private var topic = "/hfp/v2/journey/ongoing/vp/+/+/+/${routeModel.gtfsId.substringAfter(":")}/+/+/+/+/+/#"
    private var listDirection = mutableListOf<VehicleInfoSimpleModel>()
    private var listOtherDirection = mutableListOf<VehicleInfoSimpleModel>()

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
        if (progressBar1 != null) {
            progressBar1.visibility = View.VISIBLE
        }

        view.sortByClosest.setOnClickListener {
            startResponseAnimation(view.sortByClosest)
            sortByDistance()
        }
        view.bussesList.setOnItemLongClickListener { _, _, i, _ ->

                val dial = VehicleStopsDialogFragment(adapter.getItem(i))
                dial.show(fragmentManager, "VehicleStopsDialogFragment")
            true
        }

        view.bussesListOther.setOnItemLongClickListener { _, _, i, _ ->
            val dial = VehicleStopsDialogFragment(adapter1.getItem(i))
            dial.show(fragmentManager, "VehicleStopsDialogFragment")
            true
        }
        view.bussesList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, VehicleRealTimeDetailActivity::class.java).apply {
                putExtra("vehicleInfo", adapter.getItem(i))
                putExtra("stop", stopModel)
                putExtra("EndLine", secondEndingStop)
            }

            mqttService.deRegisterObserverFragment(this)
            startActivity(intent)
        }
        view.bussesListOther.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, VehicleRealTimeDetailActivity::class.java).apply {
                putExtra("vehicleInfo", adapter1.getItem(i))
                putExtra("stop", stopModel)
                putExtra("EndLine", firstEndingStop)

            }
            mqttService.deRegisterObserverFragment(this)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view
    }

    private fun startResponseAnimation(but: Button) {
        but.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_response))

    }

    override fun newMessage(message: JSONObject) {

        if (message.has("VP")) {
            val data = JSONObject(message.getString("VP"))
            val dir = data.getString("dir")
            val newBus = VehicleInfoSimpleModel(
                data.getString("veh"),
                data.getString("route"),
                data.getString("desi"),
                data.getString("lat"),
                data.getString("long"),
                "0",
                data.getString("oday"),
                data.getString("start"),
                data.getString("dir")
            )

            if (dir == "1") {
                if (listDirection.size < 1) {
                    listDirection.add(newBus)

                }
                for ((i, item) in listDirection.withIndex()) {


                    if (item.veh == data.getString(("veh"))) {
                        if (newBus.lat == "null" || newBus.longi == "null") {
                            return
                        }
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
                        if (newBus.lat == "null" || newBus.longi == "null") {
                            return
                        }
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

    private fun unsubscribe() {
        mqttService.unsubscribe(topic)
        //TODO: mqttService disconnect from the client(When trying to disconnect it crashes)
        //mqttService.disconnect()
    }

    private fun unSubsribeWithDelay() {
        Thread(Runnable {
            Timer().schedule(timerTask {
                unsubscribe()

                calcDistanceForAll()
            }, 6000)
        }).start()
    }

    private fun sortByDistance() {

        val sortedList = listDirection.sortedWith(compareBy { it.dist.toDouble() })
        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter

        val sortedList1 = listOtherDirection.sortedWith(compareBy { it.dist.toDouble() })
        adapter1 = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList1)
        view!!.bussesListOther.adapter = adapter1

    }

    private fun sortDescending() {
        val sortedList = listDirection.sortedWith(compareBy { it.veh })

        adapter = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList)
        view!!.bussesList.adapter = adapter

        val sortedList1 = listOtherDirection.sortedWith(compareBy { it.veh })

        adapter1 = BusListAdapter(this.requireContext(), R.layout.line_vehicle_list, sortedList1)
        view!!.bussesListOther.adapter = adapter1
    }

    private fun calcDistanceForAll() {

        for (item in listDirection) {

            if (item.lat != "null" && item.longi != "null") {
                val dist = locationService.calculateDistanceFromTwoPoints(
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

            if (item.lat != "null" && item.longi != "null") {
                val dist = locationService.calculateDistanceFromTwoPoints(
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
