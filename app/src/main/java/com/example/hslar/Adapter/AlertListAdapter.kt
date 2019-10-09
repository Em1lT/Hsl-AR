package com.example.hslar.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.hslar.Model.AlertModel
import com.example.hslar.R

class AlertListAdapter(var mCtx: Context, var resource: Int, var items: List<AlertModel>) :
    ArrayAdapter<AlertModel>(mCtx, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resource, null)

        val alertText: TextView = view.findViewById(R.id.alertDescription)
        val alertStartDate: TextView = view.findViewById(R.id.startDate)
        val alertEndDate: TextView = view.findViewById(R.id.endDate)
        val stopAlert: TextView = view.findViewById(R.id.stopAlert)
        val speechBubble: LinearLayout = view.findViewById(R.id.speech_bubble)
        val vehicleImage: ImageView = view.findViewById(R.id.secondImage)

        var mAlert = items[position]
        if (mAlert.stop != null) {
            stopAlert.text = mAlert.stop!!.name
            val param = speechBubble.layoutParams as RelativeLayout.LayoutParams
            param.setMargins(40, 0, 40, 0)
            speechBubble.layoutParams = param

            //CREATE OWNT SPEECHBUBBLES FOR ALL
            when {
                mAlert.stop!!.vehicleMode == "BUS" -> {
                    speechBubble.setBackgroundResource(R.drawable.speech_bubble_bus)
                    vehicleImage.setBackgroundResource(R.drawable.busimage)

                }
                mAlert.stop!!.vehicleMode == "SUBWAY" -> {
                    speechBubble.setBackgroundResource(R.drawable.speech_bubble_bus)
                    vehicleImage.setBackgroundResource(R.drawable.busimage)
                }
                mAlert.stop!!.vehicleMode == "TRAM" -> {
                    speechBubble.setBackgroundResource(R.drawable.speech_bubble_bus)
                    vehicleImage.setBackgroundResource(R.drawable.tram)
                }
                mAlert.stop!!.vehicleMode == "TRAIN" -> {
                    speechBubble.setBackgroundResource(R.drawable.speech_bubble_bus)
                    vehicleImage.setBackgroundResource(R.drawable.train)
                }
            }
        }

        alertText.text = mAlert.alertDescriptionText
        alertStartDate.text = mAlert.effectiveStartDate
        alertEndDate.text = mAlert.effectiveEndDate

        return view
    }
}
