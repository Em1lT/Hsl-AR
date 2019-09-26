package com.example.hslar.Services

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class InternalStorageService{


    fun writeOnAFile(context: Context, filename: String, data: String){
        val FILENAME = filename
        val data:String = data
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            Log.d("Main", "write success to file: $filename data: $data")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun readOnFile(context: Context, filename: String): String? {

        try {
            var fileInputStream: FileInputStream? = null
            fileInputStream = context.openFileInput(filename)
            var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }
            return stringBuilder.toString()
        } catch (e: Exception){
            return null
        }

    }
}