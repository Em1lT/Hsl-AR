package com.example.hslar.DialogFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hslar.Adapter.NextStopsAdapter
import com.example.hslar.Model.*
import com.example.hslar.R
import com.example.hslar.Services.HttpService
import kotlinx.android.synthetic.main.dialog_fragment_vehicle_stops.*
import org.json.JSONObject


@SuppressLint("ValidFragment")
class VehicleStopsDialogFragment(var vehicle: VehicleInfoSimpleModel?) : DialogFragment(){

    private lateinit var httpService: HttpService
    var list = mutableListOf<FuzzyTripModel>()
    private lateinit var adapter: NextStopsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        httpService = HttpService()
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return inflater.inflate(R.layout.dialog_fragment_vehicle_stops, container, false)
    }

    @SuppressLint("LogNotTimber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var calc = vehicle!!.start
        val timeFromMidnight = calc.substringBefore(":").toInt() * 60 *60 + calc.substringAfter(":").toInt() * 60
        Log.d("Main", "direction of the vehicle ${vehicle!!.dir}")
        var direction = if(vehicle!!.dir.toInt() == 1){
            0
        } else {
            1
        }
        Log.d("Main", "direction to fuzzyTrip: $direction")
        val json = JSONObject()
        json.put("query", "{fuzzyTrip(route:\"HSL:${vehicle!!.route}\", direction:$direction, date:\"${vehicle!!.oday}\", time: $timeFromMidnight){ stoptimesForDate {scheduledArrival realtimeArrival stop{ name }}}}")
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataRoute = JSONObject(data.getString("data"))

            val fuzzyTrip = dataRoute.getJSONObject("fuzzyTrip")
            val stopsTimesForDate = fuzzyTrip.getJSONArray("stoptimesForDate")
            Log.d("Main", "$stopsTimesForDate")
            if(stopsTimesForDate.length() > 0){
                for(i in 0 until stopsTimesForDate.length()){
                    var scheduleTemp = stopsTimesForDate.getJSONObject(i)
                    var schedule = FuzzyTripModel(
                        scheduleTemp.getLong("scheduledArrival"),
                        scheduleTemp.getLong("realtimeArrival"),
                        StopModelVerySimple(
                            scheduleTemp.getJSONObject("stop").getString("name")), null)

                    if(list.size < 1){
                        schedule.event = 1
                    }
                    list.add(schedule)


                }

                list.last().event = 2

                adapter = NextStopsAdapter(activity!!.applicationContext, R.layout.stop_single_vehicle, list)
                nextStops.adapter = adapter

            }
        }

    }

}
