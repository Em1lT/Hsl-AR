package com.example.hslar.Model

data class FuzzyTripModel (val schedulerArrival: Long, val realtimeArrival: Long, val stopModelSimple: StopModelVerySimple, var event: Int?)
data class StopModelVerySimple(val name: String)