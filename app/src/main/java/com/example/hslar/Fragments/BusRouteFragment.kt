package com.example.hslar.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.Adapter.BusRouteListAdapter
import com.example.hslar.MainActivity
import com.example.hslar.Model.RouteModel
import com.example.hslar.R
import kotlinx.android.synthetic.main.fragment_bus_route.view.*
import org.json.JSONArray


/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("ValidFragment")
class BusRouteFragment(val busRoute: JSONArray) : Fragment() {

    var list = mutableListOf<RouteModel>()
    lateinit var adapter: BusRouteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bus_route, container, false)

        // Inflate the layout for this fragment
        for (i in 0 until busRoute.length()) {
            val item = busRoute.getJSONObject(i)
            list.add(
                RouteModel(
                    item.getString("gtfsId"),
                    item.getString("shortName"),
                    item.getString("longName"),
                    item.getString("mode")
                )
            )
        }
        adapter =
            BusRouteListAdapter(this.requireContext(), R.layout.busline_list, list)
        view.buslineList.adapter = adapter


        view.buslineList.setOnItemLongClickListener { adapterView, view, i, l ->
            Log.d("Main", "LongCLick")
            true
        }
        view.buslineList.setOnItemClickListener { adapterView, view, i, l ->

            (activity as MainActivity).callbackFromRoute(list[i])
        }

        return view
    }
}

