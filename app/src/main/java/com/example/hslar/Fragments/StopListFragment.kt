package com.example.hslar.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import com.example.hslar.Adapter.StopListAdapter
import com.example.hslar.MainActivity
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel
import com.example.hslar.PopUp.DialogPopup
import com.example.hslar.R
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.LocationService
import kotlinx.android.synthetic.main.fragment_bus_route.view.buslineList
import kotlinx.android.synthetic.main.fragment_stop_list.view.*
import org.json.JSONArray
import org.json.JSONObject

/**
 *Fragment where you can choose the stop you where you want to go
 * Gets an routeModel as a parameter. With that parameter get all of the stops on the route and display it in a custom ListView.
 * Calculates the distance with LocationService. You can also filter the list by closest
 */

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("ValidFragment")
class StopListFragment(private val routeModel: RouteModel) : Fragment() {

    private lateinit var httpService: HttpService
    private lateinit var adapter: StopListAdapter
    private lateinit var locationService: LocationService

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

        view.buslineList.setOnItemLongClickListener { _, _, i, _ ->

            val loc = locationService.getYourLocation()
            val loc1 =
                locationService.createLocation(adapter.getItem(i).lat.toDouble(), adapter.getItem(i).lon.toDouble())
            if (loc != null) {
                val dial = DialogPopup(loc, loc1)
                dial.show(fragmentManager, "Dialog_popup")
            }

            true
        }
        view.buslineList.setOnItemClickListener { _, _, i, _ ->

            (activity as MainActivity).callbackFromStop(routeModel, adapter.getItem(i))
        }

        return view
    }

    private fun sortByClosestStop() {
        val sortedList = list.sortedWith(compareBy { it.dist.toDouble() })
        adapter =
            StopListAdapter(this.requireContext(), R.layout.stop_detail_list, sortedList)
        view!!.buslineList.adapter = adapter
    }

    private fun getStops(line: String) {
        val json = JSONObject()
        json.put("query", "{routes(name:\"$line\"){stops{gtfsId name lat lon zoneId code desc}}}")
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataRoute = JSONObject(data.getString("data"))
            if (JSONArray(dataRoute.getString("routes")).length() > 0) {

                val routes = JSONArray(dataRoute.getString("routes"))
                val dataStops = JSONArray(routes.getJSONObject(0).getString("stops"))

                createList(dataStops)
            } else {
                Toast.makeText(activity, "no information available", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun createList(dataStops: JSONArray) {
        for (i in 0 until dataStops.length()) {
            val item = dataStops.getJSONObject(i)
            list.add(
                StopModel(
                    item.getString("gtfsId").substringAfter(":"),
                    item.getString("name"),
                    item.getString("lat"),
                    item.getString("lon"),
                    item.getString("zoneId"),
                    item.getString("code"),
                    item.getString("desc"),
                    locationService.calculateDistance(
                        item.getString("lat").toDouble(),
                        item.getString("lon").toDouble()
                    ).toString()
                )
            )
        }
        updateAdapter()
    }

    private fun updateAdapter() {
        adapter.notifyDataSetChanged()
    }

    private fun startResponseAnimation(but: Button) {
        but.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_response))

    }

}
