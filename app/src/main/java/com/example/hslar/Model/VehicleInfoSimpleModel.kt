package com.example.hslar.Model

import java.io.Serializable

class VehicleInfoSimpleModel (
    val veh: String,
    val route: String,
    val desi: String,
    val lat: String,
    val longi: String,
    var dist: String,
    val oday: String,
    val start: String,
    val dir: String
): Serializable