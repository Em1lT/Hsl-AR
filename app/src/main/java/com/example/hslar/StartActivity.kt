package com.example.hslar

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    private var progressMax: Int = 0
    private var notificationSet = false
    private var notificationManager: NotificationManager? = null
    private lateinit var notification: NotificationCompat.Builder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager


        hsl.setOnClickListener {
           startActivity(MainActivity())
        }

        cityBikes.setOnClickListener {
            startActivity(CBActivity())
        }
    }
    private fun createNotification(notificationDistance: Int) {
        Log.d("Main", "Notification created")

    }

    private fun startActivity(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        Log.d("Main", "onDestroy StartActivity")

        super.onDestroy()
    }

    override fun onStop() {
        Log.d("Main", "onStop StartActivity")

        super.onStop()
    }

    override fun onRestart() {
        Log.d("Main", "onRestart StartActivity")

        super.onRestart()
    }

    override fun onPause() {
        Log.d("Main", "onPause StartActivity")
        super.onPause()
    }

    override fun onResume() {
        Log.d("Main", "onREsume StartActivity")
        super.onResume()
    }
}
