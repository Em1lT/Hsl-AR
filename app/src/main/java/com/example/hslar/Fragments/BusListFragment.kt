package com.example.hslar.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.Adapter.BusListAdapter
import com.example.hslar.Model.BusDetailModel
import com.example.hslar.Model.RouteModel
import com.example.hslar.R
import com.example.hslar.SingleBusDetailActivity
import com.example.hslar.Services.MqttServiceCaller
import com.example.hslpoc.Observer
import kotlinx.android.synthetic.main.fragment_bus_list.view.*
import org.json.JSONObject

@SuppressLint("ValidFragment")
class BusListFragment(val routeModel: RouteModel) : Fragment(), Observer {

    lateinit var adapter: BusListAdapter
    lateinit var mqttService: MqttServiceCaller
    var topic = "/hfp/v2/journey/ongoing/+/bus/+/+/${routeModel.gtfsId.substringAfter(":")}/+/+/+/+/+/#"
    var list = mutableListOf<BusDetailModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mqttService = MqttServiceCaller(this.requireContext(), topic)
        mqttService.registerObserverFragment(this)


        val view = inflater.inflate(R.layout.fragment_bus_list, container, false)
        Thread { mqttService.run() }.start()

        //This doesn't work. subsribe is called in MqttService
        //mqttService.subscribe(topic)

        adapter = BusListAdapter(this.requireContext(), R.layout.busline_list, list)
        view.bussesList.adapter = adapter

        view.bussesList.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this.context, SingleBusDetailActivity::class.java).apply {
                putExtra("bus", list[i])
            }
            Log.d("Main", "deregister Fragment")
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

        for(i in 0 until 300){
            if (message.has("VP")) {
                var data = JSONObject(message.getString("VP"))
                var newBus = BusDetailModel(
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
                    "VP"
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
        }
        adapter.notifyDataSetChanged()
        //TODO: mqttService disconnect from the client(When trying to disconnect it crashes)
        //mqttService.disconnect()
        mqttService.unsubscribe(topic)
    }
}
