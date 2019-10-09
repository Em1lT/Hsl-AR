package com.example.hslar.Model


data class VoiScooter(
    val id: String,
    val short: String,
    val name: String,
    val zone: Int,
    val type: String,
    val status: String,
    val bounty: Int,
    val lat: String,
    val lng: String,
    val battery: Int,
    val locked: Boolean,
    val distance: String
)