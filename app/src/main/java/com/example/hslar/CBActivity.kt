package com.example.hslar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.hslar.Model.CBStationModel
import com.example.hslar.Model.ScooterLocationModel
import com.example.hslar.Model.VoiScooter
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.HttpServiceVoi
import com.example.hslar.Services.InternalStorageService
import com.example.hslar.Services.LocationService
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.activity_cb.*
import org.json.JSONArray
import org.json.JSONObject


class CBActivity : AppCompatActivity() {

    lateinit var httpService: HttpService
    lateinit var httpServiceVoi: HttpServiceVoi
    lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService
    private lateinit var myLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cb)

        httpService = HttpService()
        httpServiceVoi = HttpServiceVoi()
        locationService = LocationService(this)


        //TODO: Animations when loading markers, when button pressed.

        rentButton.setOnClickListener {
            getBikeParkId(rentButton)
        }

        returnButton.setOnClickListener {
            getBikeParkId(returnButton)
        }

        scooterButton.setOnClickListener {
            getScooter(scooterButton)
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

                createList(brStations, view)
            }
        }
    }

    fun getScooter(view: View) {
        var json = JSONObject()
        val res = httpServiceVoi.getRequest()
        var jsonArray = JSONArray(res)

        json.put("scooters", jsonArray)
        var jsonA = json.getJSONArray("scooters")

        for (i in 0 until jsonA.length()) {
            var jsonO = jsonA.getJSONObject(i)
            var jsonAr = jsonO.getJSONArray("location")

            for (j in 0 until jsonAr.length()) {
                var item1 = jsonAr.getString(0)
                var item2 = jsonAr.getString(1)
                CbList.scooterLocationList.add(
                    ScooterLocationModel(
                        item1,
                        item2
                    )
                )
            }
        }
        createScooterList(jsonArray, view)
    }

    fun createScooterList(scooters: JSONArray, view: View) {
        for (i in 0 until scooters.length()) {
            val item = scooters.getJSONObject(i)
            var locations = CbList.scooterLocationList[i]
            //TODO: CALCULATES THE DISTANCE BETWEEN
            CbList.scooterList.add(
                VoiScooter(
                    item.getString("id"),
                    item.getString("short"),
                    item.getString("name"),
                    item.getInt("zone"),
                    item.getString("type"),
                    item.getString("status"),
                    item.getInt("bounty"),
                    locations.lat,
                    locations.lng,
                    item.getInt("battery"),
                    item.has("locked")
                    //locationService.calculateDistance(item.getString("lat").toDouble(), item.getString("lon").toDouble()).toString()
                )
            )
        }
        if (view == scooterButton) {
            val intent = Intent(this, CBLocActivity::class.java).apply {
                putExtra("button", "scooter")
            }
            startActivity(intent)
        }
    }

    fun createList(bikeRentals: JSONArray, view: View) {
        for (i in 0 until bikeRentals.length()) {
            val item = bikeRentals.getJSONObject(i)
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
                    item.has("allowDropoff"),
                    locationService.calculateDistance(item.getString("lat").toDouble(), item.getString("lon").toDouble()).toString()
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

    private fun getYourLocation(){
        var data = internalStorageService.readOnFile(this,"location.txt")
        var lat: Double
        var long: Double
        if(data!!.isNotEmpty()) {
            lat = data!!.substringBefore(":").toDouble()
            long = data.substringAfter(":").toDouble()
            myLocation = LatLng(lat, long)
        }
    }
}

// Singleton object for list of different models
object CbList {
    var cbList = mutableListOf<CBStationModel>()
    var scooterList = mutableListOf<VoiScooter>()
    var scooterLocationList = mutableListOf<ScooterLocationModel>()
}
