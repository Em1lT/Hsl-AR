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


//TODO: Create strings values for all the texts
//TODO: Create strings values for different languages
//TODO: Databinding for adapters
//TODO: clean lint errors
//TODO: When closing application is causes an crash... (doesn't affect run of the application. no noticeable to user)
//TODO: Create detailed comments for every class
//TODO: Some kind of notification when bus is close

class MainActivity : AppCompatActivity() {

    lateinit var httpService: HttpService
    lateinit var  locationService: LocationService

    private val PERMISSION_CODE = 10

    //TODO: CHANGE ALL OF THE MODELS TO THE RIGHT VALUE
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED){

            val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permission, PERMISSION_CODE)
        }

        httpService = HttpService()
        locationService = LocationService(this)


        bCheck.setOnClickListener {

            startResponseAnimation(bCheck)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
               getLocation()
               getbusId(busline.text.toString())
            } else {
                //TODO: Create notification service
            }
        }
    }
    fun startResponseAnimation(button: Button){

        button.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_response))
    }
    fun getLocation(){
        locationService.getLocation()
    }
    fun getbusId(line: String) {
        var json = JSONObject()
        json.put("query", "{routes(name:\"$line\"){gtfsId shortName longName mode}}")
        val res = httpService.postRequest(json)
        var data = JSONObject(res)

        if(data.has("data")){
            var dataRoute = JSONObject(data.getString("data"))
            if(JSONArray(dataRoute.getString("routes")).length() > 0){

                var routes = JSONArray(dataRoute.getString("routes"))

                if(routes.length() > 0){
                   startFragment(BusRouteFragment(routes))
                }
            }else{
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
    fun callbackFromStop(routeModel: RouteModel, stopModel: StopModel){
        startFragment(
            BusListFragment(
                routeModel, stopModel
            )
        )
    }
    private fun startFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}
