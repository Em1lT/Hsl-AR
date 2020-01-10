package com.example.hslar.Model

data class FuzzyTripModel (val schedulerArrival: Int, val realtimeArrival: Int, val stopModelSimple: StopModelVerySimple, var firstOrLast: Int?, var active: Boolean?)
data class StopModelVerySimple(val name: String)