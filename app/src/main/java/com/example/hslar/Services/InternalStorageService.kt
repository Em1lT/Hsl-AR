package com.example.hslar.Services

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

/**
 * 07.09.2018
 * Is used to write data to a file and read data from the file... for example used to save the location to a file
 *
 */
@Suppress("NAME_SHADOWING")
class InternalStorageService {


    fun writeOnAFile(context: Context, filename: String, data: String) {
        val FILENAME = filename
        val data: String = data
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            Log.d("Main", "write success to file: $filename data: $data")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readOnFile(context: Context, filename: String): String? {

        return try {
            val fileInputStream: FileInputStream? = context.openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }
            stringBuilder.toString()
        } catch (e: Exception) {
            null
        }

    }
}