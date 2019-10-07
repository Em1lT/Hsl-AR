package com.example.hslar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import com.example.hslar.Fragments.BusListFragment
import com.example.hslar.Fragments.BusRouteFragment
import com.example.hslar.Fragments.StopListFragment
import com.example.hslar.Model.RouteModel
import com.example.hslar.Model.StopModel
import com.example.hslar.Services.HttpService
import com.example.hslar.Services.LocationService
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 07.09.2019
 * When choosing the upper button in the StartActivity you will come here. Contains 3 fragments.
 * This activity gets the Route, stops and current ongoing busses, trams, trains or metros.
 * Every step has it's own activity/fragment
 * First we get the route id with current activity. We also get the current location with googleFusedLocations API
 * Then on the function getId() we get the information about the route. We call hsl api with HTTPService.
 * Finally we pass the data BusRouteFragment
 */

//TODO: Create strings values for all the texts
//TODO: Create detailed comments for every class

class MainActivity : AppCompatActivity() {

    private lateinit var httpService: HttpService
    private lateinit var locationService: LocationService

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        httpService = HttpService()
        locationService = LocationService(this)


        bCheck.setOnClickListener {

            startResponseAnimation(bCheck)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getLocation()
                getId(busline.text.toString())
            } else {
                //TODO: Create notification service
            }
        }
    }

    private fun startResponseAnimation(button: Button) {

        button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_response))
    }

    private fun getLocation() {
        locationService.getLocation()
    }

    private fun getId(line: String) {
        val json = JSONObject()
        json.put("query", "{routes(name:\"$line\"){gtfsId shortName longName mode}}")
        val res = httpService.postRequest(json)
        val data = JSONObject(res)

        if (data.has("data")) {
            val dataRoute = JSONObject(data.getString("data"))
            if (JSONArray(dataRoute.getString("routes")).length() > 0) {

                val routes = JSONArray(dataRoute.getString("routes"))

                if (routes.length() > 0) {
                    startFragment(BusRouteFragment(routes))
                }
            } else {
                Toast.makeText(this, "no route for this number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun callbackFromRoute(routeModel: RouteModel) {
        startFragment(
            StopListFragment(
                routeModel
            )
        )
    }

    fun callbackFromStop(routeModel: RouteModel, stopModel: StopModel) {
        startFragment(
            BusListFragment(
                routeModel, stopModel
            )
        )
    }

    private fun startFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}
