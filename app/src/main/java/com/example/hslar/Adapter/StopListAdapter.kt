package com.example.hslar.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.Model.StopModel
import com.example.hslar.R
import com.example.hslar.Services.LocationService
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class StopListAdapter(var mCtx: Context, var resource: Int, var items: List<StopModel>)
    : ArrayAdapter<StopModel>(mCtx, resource, items){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resource, null)

        val stopId: TextView = view.findViewById(R.id.stopId)
        val desc: TextView = view.findViewById(R.id.stopDesc)
        val zoneId: TextView = view.findViewById(R.id.zoneId)
        val dist: TextView = view.findViewById(R.id.distanceFromMe)

        var mItems: StopModel = items[position]


        stopId.text = mItems.code
        desc.text = mItems.name
        zoneId.text = mItems.zoneId
        if(mItems.dist.toDouble() > 1000){
            dist.text = "${"%.2f".format(mItems.dist.toDouble() / 1000)} km to nearest stop"
        } else {
            dist.text = "${(mItems.dist.toDouble()).roundToInt()} meters to nearest stop"
        }
        return view
    }

}