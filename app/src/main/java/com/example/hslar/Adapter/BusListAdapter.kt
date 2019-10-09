package com.example.hslar.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.hslar.Model.VehicleInfoSimpleModel
import com.example.hslar.R

class BusListAdapter (var mCtx: Context, var resource: Int, var items: List<VehicleInfoSimpleModel>)
    :ArrayAdapter<VehicleInfoSimpleModel>(mCtx, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view:View = layoutInflater.inflate(resource, null)

        val vehNum: TextView = view.findViewById(R.id.vehNum)
        val distanceFromStop: TextView = view.findViewById(R.id.distanceFromStop)

        var mItems: VehicleInfoSimpleModel = items[position]

        vehNum.text = mItems.veh
        if(mItems.dist.toInt() > 1000){
            distanceFromStop.text = "${"%.2f".format(mItems.dist.toDouble() / 1000)} km away"
        } else {
            distanceFromStop.text = "${mItems.dist} meters away"
        }
        return view
    }
}