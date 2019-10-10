package com.example.hslar.Model

data class CBStationModel(
    val id: String,
    val stationId: String,
    val name: String,
    val bikesAvailable: Int,
    val spacesAvailable: Int,
    val lat: String,
    val lon: String,
    val state: String,
    val allowDrop: Boolean,
    val dist: String
)