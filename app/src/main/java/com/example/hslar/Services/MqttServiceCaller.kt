package com.example.hslar.Services

import android.content.Context
import android.support.v4.app.Fragment
import android.util.Log
import android.widget.Toast
import com.example.hslar.Fragments.BusListFragment
import com.example.hslar.SingleBusDetailActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject

/**
 * 07.09.2019
 * Using to communicate to the mqtt service. Connecting to the client, subsricing to it. disconnet & unsubscribe.
 * This has more explained because this is complex
 */
class MqttServiceCaller(val context: Context, val topic: String) : Runnable {

    val TAG = "Main"
    private val observers = mutableSetOf<BusListFragment>()
    private val observers1 = mutableSetOf<SingleBusDetailActivity>()
    private val clientId: String = MqttClient.generateClientId()
    private var client = MqttAndroidClient(context, "tcp://mqtt.hsl.fi:1883", clientId)
    var connection: Boolean = false

    override fun run() {
        clientConnect()
    }

    //Connect to the mqtt service, ActionListener listens if call is success or failure
    private fun clientConnect() {
        Log.d(TAG, "connecting to client")

        try {
            var token = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    connection = true
                    Log.d(TAG, "Mqtt success")
                    subscribe(topic)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure $exception")

                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        client.setCallback(object : MqttCallback {
            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }

            override fun connectionLost(cause: Throwable) {
                connection = false
                Log.d(TAG, "connection lost")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                messageReceived(message)
            }

        })
    }

    //disconnect from the service. if you are not subscribed service will disconnect after some time. also a listner for the result
    fun disconnect() {
        try {
            val disconToken = client.disconnect()
            disconToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // we are now successfully disconnected
                    Log.d(TAG, "client disconnected")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.d(TAG, "client disconnect failed $exception")
                    // something went wrong, but probably we are disconnected anyway
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    //When message is received call the observers that there is a new message incoming.
    fun messageReceived(message: MqttMessage) {

        val json = JSONObject(message.toString())
        for (items in observers) {
            items.newMessage(json)
        }
        for (items in observers1) {
            items.newMessage(json)
        }
    }

    //When connected to the message broker you can
    fun subscribe(topic: String) {
        val qos = 1
        try {
            val subToken = client.subscribe(topic, qos)
            subToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // The message was published
                    val toast = Toast.makeText(context, "subscribe success", Toast.LENGTH_SHORT)
                    toast.show()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.d(TAG, "$exception")
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun unsubscribe(topic: String) {

        try {
            val unsubToken = client.unsubscribe(topic)
            unsubToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // The subscription could successfully be removed from the client
                    Log.d("Main", "unsubscribe success")
                    val toast = Toast.makeText(context, "unsubscribe success", Toast.LENGTH_SHORT)
                    toast.show()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.d("Main", "unsubscribe failed $exception")
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun registerObserverFragment(observer: BusListFragment) {
        observers.add(observer)
    }

    fun deRegisterObserverFragment(observer: Fragment) {
        observers.remove(observer)
    }

    fun registerObserverActivity(observer: SingleBusDetailActivity) {
        observers1.add(observer)
    }

    fun deRegisterObserverActivity(observer: SingleBusDetailActivity) {
        observers1.remove(observer)
    }
}
/*FROM MESSAGERECEIVED

       /* if(json.has("VP")){
            Log.d(TAG, "VP")
            var data = JSONObject(json.getString("VP"))
            var newBus = Model(data.getString("desi"), data.getString("dir"), data.getString("oper"), data.getString("veh"), data.getString("tst"), data.getString("tsi"), data.getString("spd"), data.getString("hdg"), data.getString("lat"), data.getString("long"), data.getString("acc"), data.getString("odo"), data.getString("drst"), data.getString("drst"), data.getString("jrn"), data.getString("line"), data.getString("start"), data.getString("loc"), data.getString("stop"), data.getString("route"), data.getString("occu"))

            if(list.size < 1){
                list.add(newBus)
            }
            for((i, item) in list.withIndex()){
                if(item.veh == data.getString(("veh"))){
                    list[i] = Model(data.getString("desi"), data.getString("dir"), data.getString("oper"), data.getString("veh"), data.getString("tst"), data.getString("tsi"), data.getString("spd"), data.getString("hdg"), data.getString("lat"), data.getString("long"), data.getString("acc"), data.getString("odo"), data.getString("drst"), data.getString("drst"), data.getString("jrn"), data.getString("line"), data.getString("start"), data.getString("loc"), data.getString("stop"), data.getString("route"), data.getString("occu"))
                    adapter.notifyDataSetChanged()
                    return
                }
            }
            list.add(newBus)
            adapter.notifyDataSetChanged()
        }

        if(json.has("VJOUT")){
            Log.d(TAG, "VJOUT")
            for((i, item) in list.withIndex()){
                var data = JSONObject(json.getString("VJOUT"))
                if(item.veh == data.getString(("veh"))){
                    list.removeAt(i)
                    adapter.notifyDataSetChanged()
                    return
                }
            }
        }*/

    fun connectToHSL(client: MqttAndroidClient, context: Context) {
        try {
            val token = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    subscribe(client, context)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure $exception")

                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        client.setCallback(object : MqttCallback {
            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }

            override fun connectionLost(cause: Throwable) {
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                messageReceived(client, message)
            }

        })

    }
    //UN- & SUBSCRIBE

     fun subscribe(client:  MqttAndroidClient){
        val topic = "/hfp/v2/journey/+/vp/bus/+/+/2114/#"
        val qos = 1
        try {
            val subToken = client.subscribe(topic, qos)
            subToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // The message was published
                    val toast = Toast.makeText(applicationContext, "subscribe success", Toast.LENGTH_SHORT)
                    toast.show()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }
    fun unsubscribe(client:  MqttAndroidClient){


        val topic = "/hfp/v2/journey/+/vp/bus/+/+/2114/#"
        try {
            val unsubToken = client.unsubscribe(topic)
            unsubToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // The subscription could successfully be removed from the client
                    val toast = Toast.makeText(applicationContext, "unsubscribe success", Toast.LENGTH_SHORT)
                    toast.show()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    Log.d("Main", "unsubscribe failed $exception")
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun connecttoHSL(client: MqttAndroidClient, adapter: ScheduleAdapter) {
        try {
            val token = client.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    //subscribe(client)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure $exception")

                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        client.setCallback(object : MqttCallback {
            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }

            override fun connectionLost(cause: Throwable) {
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
              messageReceived(client, adapter, message)
            }

        })

    }
 */
/*
   fun messageReceived(client: MqttAndroidClient, adapter: ScheduleAdapter, message: MqttMessage){
       var json = JSONObject(message.toString())

       if(json.has("VP")){
           Log.d(TAG, "VP")
           var data = JSONObject(json.getString("VP"))
           var newBus = Model(data.getString("desi"), data.getString("dir"), data.getString("oper"), data.getString("veh"), data.getString("tst"), data.getString("tsi"), data.getString("spd"), data.getString("hdg"), data.getString("lat"), data.getString("long"), data.getString("acc"), data.getString("odo"), data.getString("drst"), data.getString("drst"), data.getString("jrn"), data.getString("line"), data.getString("start"), data.getString("loc"), data.getString("stop"), data.getString("route"), data.getString("occu"))

           if(list.size < 1){
               list.add(newBus)
           }
           for((i, item) in list.withIndex()){
               if(item.veh == data.getString(("veh"))){
                   list[i] = Model(data.getString("desi"), data.getString("dir"), data.getString("oper"), data.getString("veh"), data.getString("tst"), data.getString("tsi"), data.getString("spd"), data.getString("hdg"), data.getString("lat"), data.getString("long"), data.getString("acc"), data.getString("odo"), data.getString("drst"), data.getString("drst"), data.getString("jrn"), data.getString("line"), data.getString("start"), data.getString("loc"), data.getString("stop"), data.getString("route"), data.getString("occu"))
                   adapter.notifyDataSetChanged()
                   return
               }
           }
           list.add(newBus)
           adapter.notifyDataSetChanged()
       }

       if(json.has("VJOUT")){
           Log.d(TAG, "VJOUT")
           for((i, item) in list.withIndex()){
               var data = JSONObject(json.getString("VJOUT"))
               if(item.veh == data.getString(("veh"))){
                   list.removeAt(i)
                   adapter.notifyDataSetChanged()
                   return
               }
           }
       }
   }*/
