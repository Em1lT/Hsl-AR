package com.example.hslar.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.hslar.Model.BusSimpleModel
import com.example.hslar.R

class BusListAdapter (var mCtx: Context, var resource: Int, var items: List<BusSimpleModel>)
    :ArrayAdapter<BusSimpleModel>(mCtx, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view:View = layoutInflater.inflate(resource, null)

        val mBusLineNum: TextView = view.findViewById(R.id.busline)

        var mItems: BusSimpleModel = items[position]

        mBusLineNum.text = mItems.veh

        return view
    }
}