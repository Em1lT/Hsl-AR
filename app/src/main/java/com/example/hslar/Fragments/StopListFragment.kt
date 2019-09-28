package com.example.hslar.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import com.example.hslar.Adapter.BusListAdapter
import com.example.hslar.Adapter.BusRouteListAdapter
import com.example.hslar.Adapter.StopListAdapter
import com.example.hslar.MainActivity
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel

import com.example.hslar.R
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.LocationService
import kotlinx.android.synthetic.main.fragment_bus_list.view.*
import kotlinx.android.synthetic.main.fragment_bus_route.view.*
import kotlinx.android.synthetic.main.fragment_bus_route.view.buslineList
import kotlinx.android.synthetic.main.fragment_stop_list.view.*
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("ValidFragment")
class StopListFragment(val routeModel: RouteModel) : Fragment() {

    lateinit var httpService: HttpService
    lateinit var adapter: StopListAdapter
    lateinit var locationService: LocationService

    var list = mutableListOf<StopModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        httpService = HttpService()
        locationService = LocationService(this.requireContext())
        val view = inflater.inflate(R.layout.fragment_stop_list, container, false)
        adapter =
            StopListAdapter(this.requireContext(), R.layout.stop_detail_list, list)
        view!!.buslineList.adapter = adapter


        getStops(routeModel.shortName)
        view.sortByClosestStop.setOnClickListener {
            startResponseAnimation(view.sortByClosestStop)
            sortByClosestStop()
        }
        view.buslineList.setOnItemClickListener { adapterView, view, i, l ->

            (activity as MainActivity).callbackFromStop(routeModel, adapter.getItem(i))
        }

        return view
    }
    private fun sortByClosestStop(){
        var sortedList = list.sortedWith(compareBy { it.dist.toDouble()})
        adapter =
            StopListAdapter(this.requireContext(), R.layout.stop_detail_list, sortedList)
        view!!.buslineList.adapter = adapter
    }

    private fun getStops(line: String){
        var json = JSONObject()
        json.put("query", "{routes(name:\"$line\"){stops{gtfsId name lat lon zoneId code desc}}}")
        val res = httpService.postRequest(json)
        var data = JSONObject(res)

        if(data.has("data")){
            var dataRoute = JSONObject(data.getString("data"))
            if(JSONArray(dataRoute.getString("routes")).length() > 0){

                var routes = JSONArray(dataRoute.getString("routes"))
                var dataStops = JSONArray(routes.getJSONObject(0).getString("stops"))

                createList(dataStops)
            }else{
                Toast.makeText(activity, "no information available", Toast.LENGTH_SHORT)
            }
        }
    }
    fun createList(dataStops: JSONArray) {
        for (i in 0 until dataStops.length()) {
            val item = dataStops.getJSONObject(i)
            Log.d("Main", item.getString("code"))
                list.add(StopModel(
                    item.getString("gtfsId").substringAfter(":"),
                    item.getString("name"),
                    item.getString("lat"),
                    item.getString("lon"),
                    item.getString("zoneId"),
                    item.getString("code"),
                    item.getString("desc"),
                    locationService.calculateDistance(item.getString("lat").toDouble(), item.getString("lon").toDouble()).toString()
                ))
        }
        updateAdapter()
    }
    fun updateAdapter(){
       adapter.notifyDataSetChanged()
    }
    fun  startResponseAnimation(but: Button){
        but.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_response))

    }

}
