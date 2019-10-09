package com.example.hslar

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_start.*

/**
 * 07.09.2019
 * The Activity where the app starts.
 * Contains 2 buttons to choose from.
 */
class StartActivity : AppCompatActivity() {

    private var notificationManager: NotificationManager? = null
    private val permissionCode = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {

            val permission =
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(permission, permissionCode)
        }

        notificationManager =
            getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager


        hsl.setOnClickListener {
           startActivity(MainActivity())
        }

        cityBikes.setOnClickListener {
            startActivity(CBActivity())
        }
        bNotific.setOnClickListener {
            startActivity(AdviseNotificationActivity())
        }
    }

    private fun startActivity(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }
}
