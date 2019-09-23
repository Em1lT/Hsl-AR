package com.example.hslar.Services

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class HttpService {

    val TIMEOUT = 10*1000
    val serverURL: String = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"


    fun postRequest(queryObject: JSONObject): String? {
        val res = AsyncTaskHandle(queryObject).execute()
        return res.get()
    }
    inner class AsyncTaskHandle(private val queryObject: JSONObject) : AsyncTask<String, String, String>(){

        override fun doInBackground(vararg url: String?): String {

            var respond = "";
            val url = URL(serverURL)
            val httpClient = url.openConnection() as HttpURLConnection

            httpClient.readTimeout = TIMEOUT
            httpClient.connectTimeout = TIMEOUT
            httpClient.requestMethod = "POST"

            httpClient.instanceFollowRedirects = false
            httpClient.doOutput = true
            httpClient.doInput = true
            httpClient.useCaches = false
            httpClient.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            try {
                httpClient.connect()
                val os = httpClient.getOutputStream()
                val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                writer.write(queryObject.toString())
                writer.flush()
                writer.close()
                os.close()

                if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                    val stream = BufferedInputStream(httpClient.inputStream)
                    respond = readStream(inputStream = stream)

                } else {
                    println("ERROR ${httpClient.responseCode}")
                }

            } catch (exception: Exception) {
                Log.d("MainActivity", "here in error: $exception")
            }
            return respond
        }
        fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
        }
    }

}