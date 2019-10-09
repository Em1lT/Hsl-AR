package com.example.hslar

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hslar.Adapter.AlertListAdapter
import com.example.hslar.Model.AlertModel
import com.example.hslar.Model.StopModelSimple
import com.example.hslar.Services.HttpService
import kotlinx.android.synthetic.main.activity_advise_notification.*
import org.json.JSONArray
import org.json.JSONObject

class AdviseNotificationActivity : AppCompatActivity() {

    private lateinit var httpService: HttpService
    var list = mutableListOf<AlertModel>()
    private lateinit var adapter: AlertListAdapter

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advise_notification)

        httpService = HttpService()


        val json = JSONObject()
        json.put("query", "{alerts {alertDescriptionText effectiveStartDate effectiveEndDate alertUrl stop {name lat lon vehicleMode}}}")
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataRoute = JSONObject(data.getString("data"))
            if (JSONArray(dataRoute.getString("alerts")).length() > 0) {

                val alerts = JSONArray(dataRoute.getString("alerts"))
                for (i in 0 until 100) {
                    val alert = alerts.getJSONObject(i)
                    var epochStart = alert.getLong("effectiveStartDate")
                    var epochEnd = alert.getLong("effectiveStartDate")
                    val startingDate = turnToDate(epochStart)
                    val endingDate = turnToDate(epochEnd)

                    if(alert.getString("stop") != "null"){
                        val stop = alert.getJSONObject("stop")
                        val stopModel = StopModelSimple(stop.getString("name"),
                            stop.getDouble("lat"),
                            stop.getDouble("lon"),
                            stop.getString("vehicleMode"))

                        list.add(
                            AlertModel(
                                (alert.getString("alertDescriptionText")),
                                startingDate,
                                endingDate,
                                alert.getString("alertUrl"),
                                stopModel)
                            )
                    } else {
                        list.add(
                            AlertModel(
                                (alert.getString("alertDescriptionText")),
                                startingDate,
                                endingDate,
                                alert.getString("alertUrl"),
                                null
                            )
                        )
                    }
                }
                adapter =
                    AlertListAdapter(this, R.layout.alert_list, list)
                serviceList.adapter = adapter


            } else {
                Toast.makeText(this, "no alerts", Toast.LENGTH_SHORT).show()
            }
        }

    }
    @SuppressLint("SimpleDateFormat")
    fun turnToDate(epoch: Long): String{
        return java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(epoch * 1000))
    }
}
