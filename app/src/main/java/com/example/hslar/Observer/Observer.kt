package com.example.hslpoc

import org.json.JSONObject

interface Observer {

    fun newMessage(message: JSONObject)
}
