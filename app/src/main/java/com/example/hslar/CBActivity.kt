package com.example.hslar

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
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

    private lateinit var httpService: HttpService
    private lateinit var httpServiceVoi: HttpServiceVoi
    private lateinit var locationService: LocationService
    private lateinit var internalStorageService: InternalStorageService
    private lateinit var myLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cb)

        httpService = HttpService()
        httpServiceVoi = HttpServiceVoi()

        internalStorageService = InternalStorageService()
        locationService = LocationService(this)

        // Refreshes location.txt file to get current location.
        locationService.getLocation()
        myLocation = LatLng(locationService.getYourLocation())

        rentButton.setOnClickListener {
            startResponseAnimation(rentButton)
            getBikeParkId(rentButton)
        }

        returnButton.setOnClickListener {
            startResponseAnimation(returnButton)
            getBikeParkId(returnButton)
        }

        scooterButton.setOnClickListener {
            startResponseAnimation(scooterButton)
            getScooter(scooterButton)
        }
    }

    // Starts an animation when button is pressed
    private fun startResponseAnimation(button: Button){
        button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_response))
    }

    // Sends a http post request with a QraphiQL quary to receive json data string of hsl city
    // bikes and reforms it to json array.
    private fun getBikeParkId(view: View) {
        val json = JSONObject()
        json.put(
            "query",
            "{bikeRentalStations{id stationId lon lat name spacesAvailable bikesAvailable state}}"
        )
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataCityBike = JSONObject(data.getString("data"))
            if (JSONArray(dataCityBike.getString("bikeRentalStations")).length() > 0) {
                val brStations = JSONArray(dataCityBike.getString("bikeRentalStations"))

                createList(brStations, view)
            }
        }
    }

    // Sends a http get request to get json data string of all the scooters in the helsinki area
    // and parses the json. Values are added from json file to list of ScooterLocationModels and
    // to a json array for VoiScooter list creation.
    private fun getScooter(view: View) {
        val json = JSONObject()
        val res = httpServiceVoi.getRequest()
        val jsonArray = JSONArray(res)

        json.put("scooters", jsonArray)
        val jsonA = json.getJSONArray("scooters")

        for (i in 0 until jsonA.length()) {
            val jsonO = jsonA.getJSONObject(i)
            val jsonAr = jsonO.getJSONArray("location")

            for (j in 0 until jsonAr.length()) {
                val item1 = jsonAr.getString(0)
                val item2 = jsonAr.getString(1)
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

    // Creates a list of VoiScooters from the json array and adds the locations from the scooter
    // location list. Starts the map activity when list is done.
    private fun createScooterList(scooters: JSONArray, view: View) {
        for (i in 0 until scooters.length()) {
            val item = scooters.getJSONObject(i)
            val locations = CbList.scooterLocationList[i]
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
                    item.has("locked"),
                    locationService.calculateDistance(locations.lat.toDouble(), locations.lng.toDouble()).toString()
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

    // Creates a list of CBStationModels from the json array response and starts next activity.
    private fun createList(bikeRentals: JSONArray, view: View) {
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
                    item.has("allowDrop"),
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
}

// Singleton object for list of different models.
object CbList {
    var cbList = mutableListOf<CBStationModel>()
    var scooterList = mutableListOf<VoiScooter>()
    var scooterLocationList = mutableListOf<ScooterLocationModel>()
}
