package com.example.hslar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import android.support.v4.app.Fragment
import com.example.hslar.Fragments.BusListFragment
import com.example.hslar.Fragments.BusRouteFragment
import com.example.hslar.Model.RouteModel
import com.example.hslar.Services.HttpService


//TODO: Create strings values for all the texts
//TODO: Create strings values for different languages
//TODO: Databinding for adapters
//TODO: Better UI, similar to HSL, check https://www.hsl.fi/, animations also
//TODO: clean lint errors
//TODO: When closing application is causes an crash... (doesn't affect run of the application. no noticeable to user)
//TODO: Create detailed comments for every class
//TODO: Some kind of notification when bus is close

class MainActivity : AppCompatActivity() {

    lateinit var httpService: HttpService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        httpService = HttpService()

        bCheck.setOnClickListener {
            getbusId(busline.text.toString())
        }
    }

    fun getbusId(busLine: String) {
        var json = JSONObject()
        json.put("query", "{routes(name:\"$busLine\"){gtfsId shortName longName mode}}")
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
                Toast.makeText(this, "no bus route for this number", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun callback(routeModel: RouteModel) {
        startFragment(
            BusListFragment(
                routeModel
            )
        )
    }
    private fun startFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }
}
