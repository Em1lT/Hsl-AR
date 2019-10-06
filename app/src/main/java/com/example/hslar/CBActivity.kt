package com.example.hslar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.hslar.Model.CBStationModel
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.LocationService
import kotlinx.android.synthetic.main.activity_cb.*
import org.json.JSONArray
import org.json.JSONObject


class CBActivity : AppCompatActivity() {

    lateinit var httpService: HttpService
    lateinit var locationService: LocationService
    //var list = mutableListOf<CBStationModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cb)

        httpService = HttpService()
        //locationService = LocationService(this)

        //TODO: Animations when loading markers, when button pressed.

        rentButton.setOnClickListener {
            getBikeParkId(rentButton)
        }

        returnButton.setOnClickListener {
            getBikeParkId(returnButton)
        }
    }

    fun getBikeParkId(view: View) {
        var json = JSONObject()
        json.put(
            "query",
            "{bikeRentalStations{id stationId lon lat name spacesAvailable bikesAvailable state}}"
        )
        val res = httpService.postRequest(json)
        var data = JSONObject(res)

        if (data.has("data")) {
            var dataCityBike = JSONObject(data.getString("data"))
            if (JSONArray(dataCityBike.getString("bikeRentalStations")).length() > 0) {

                var brStations = JSONArray(dataCityBike.getString("bikeRentalStations"))

                Log.d("TAG", brStations.toString())
                createList(brStations, view)

            }
        }
    }

    fun createList(bikeRentals: JSONArray, view: View) {
        for (i in 0 until bikeRentals.length()) {
            val item = bikeRentals.getJSONObject(i)
            //TODO: CALCULATES THE DISTANCE BETWEEN
            CbList.cbList.add(
                CBStationModel(
                    item.getString("id"),
                    item.getString("stationId"),
                    item.getString("name"),
                    item.getInt("bikesAvailable"),
                    item.getInt("spacesAvailable"),
                    item.getString("lat"),
                    item.getString("lon"),
                    item.getString("state"),
                    item.has("allowDropoff")
                )
            )
        }
        if (view == rentButton) {
            val intent = Intent(this, CBLocActivity::class.java).apply {
                putExtra("button", "rent")
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, CBLocActivity::class.java).apply {
                putExtra("button", "return")
            }
            startActivity(intent)
        }
    }
}

// Singleton object for list of CBStationModels
object CbList {
    var cbList = mutableListOf<CBStationModel>()
}