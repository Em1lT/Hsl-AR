package com.example.hslar.Model

import java.io.Serializable

data class StopModelSimple(var name: String, var lat: Double, var lon: Double, val vehicleMode: String): Serializable