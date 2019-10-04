package com.example.hslar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)


        hsl.setOnClickListener {
            startActivity(MainActivity())
        }

        cityBikes.setOnClickListener {
            startActivity(CBActivity())
        }
    }
    private fun startActivity(activity: Activity){
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
    }
}
