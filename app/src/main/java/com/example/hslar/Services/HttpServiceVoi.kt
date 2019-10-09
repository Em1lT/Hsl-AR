package com.example.hslar.Services

import android.os.AsyncTask
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class HttpServiceVoi{

    val serverURL: String = "https://api.voiapp.io/v1/vehicle/status/ready?lat=60.169908&lng=24.938554"

    fun getRequest(): String? {
        val res = AsyncTaskHandle().execute()
        return res.get()
    }
    inner class AsyncTaskHandle : AsyncTask<String, String, String>(){

        override fun doInBackground(vararg url: String?): String {

            var respond = ""
            val url = URL(serverURL)

            try {
                val httpClient = url.openConnection() as HttpURLConnection
                httpClient.requestMethod = "GET"
                val iStream: InputStream = httpClient.inputStream
                val json = iStream.bufferedReader().use { it.readText() }
                val result = StringBuilder()
                result.append(json)
                respond = result.toString()
            } catch (exception: Exception) {
                Log.d("MainActivity", "here in error: $exception")
            }
            Log.d("FUK", respond)
            return respond
        }
    }
}