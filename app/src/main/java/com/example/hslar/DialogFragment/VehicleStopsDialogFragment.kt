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
import kotlinx.android.synthetic.main.dialog_fragment_vehicle_stops.view.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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

        val calc = vehicle!!.start
        val timeInSeconds = getTime()

        val timeFromMidnight = calc.substringBefore(":").toInt() * 60 *60 + calc.substringAfter(":").toInt() * 60
        val direction = if(vehicle!!.dir.toInt() == 1){
            0
        } else {
            1
        }
        val json = JSONObject()
        json.put("query", "{fuzzyTrip(route:\"HSL:${vehicle!!.route}\", direction:$direction, date:\"${vehicle!!.oday}\", time: $timeFromMidnight){ stoptimesForDate {scheduledArrival realtimeArrival stop{ name }}}}")
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataRoute = JSONObject(data.getString("data"))

            val fuzzyTrip = dataRoute.getJSONObject("fuzzyTrip")
            val stopsTimesForDate = fuzzyTrip.getJSONArray("stoptimesForDate")
            if(stopsTimesForDate.length() > 0){
                for(i in 0 until stopsTimesForDate.length()){

                    val scheduleTemp = stopsTimesForDate.getJSONObject(i)
                    val schedule = FuzzyTripModel(
                        scheduleTemp.getInt("scheduledArrival"),
                        scheduleTemp.getInt("realtimeArrival"),
                        StopModelVerySimple(
                            scheduleTemp.getJSONObject("stop").getString("name")), null, null)

                    if(list.size < 1){
                        schedule.firstOrLast = 1
                    }
                    list.add(schedule)
                }

                adapter = NextStopsAdapter(activity!!.applicationContext, R.layout.stop_single_vehicle, list)
                nextStops.adapter = adapter

                var closest = closest(timeInSeconds, list)
                for((i, item) in list.withIndex()){
                    if(closest == item){
                        item.active = true
                        view.nextStops.setSelection(i)
                    }
                }

                list.last().firstOrLast = 2
                adapter.notifyDataSetChanged()
            }
        }

    }
    fun getTime(): Int {
        val myTime= LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val minutes = myTime.substringAfter(":")
        val hours = myTime.substringBefore(":")
        return minutes.toInt() * 60 + hours.toInt() * 60 * 60
    }
    @SuppressLint("LogNotTimber")
    fun closest(of: Int, list: MutableList<FuzzyTripModel>): FuzzyTripModel? {
        var closest: FuzzyTripModel? = null

        for ((v) in list.withIndex()) {

            val sced = list[v].schedulerArrival
            if(of > sced){
                closest = list[v]
            }
        }
        Log.d("Main", "$closest")
        return closest
    }

}
