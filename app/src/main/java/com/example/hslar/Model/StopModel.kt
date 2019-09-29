package com.example.hslar.Model

import java.io.Serializable

data class StopModel(val gtfsId: String, val name: String, val lat: String, val lon: String, val zoneId: String, val code: String, val desc: String, val dist: String): Serializable