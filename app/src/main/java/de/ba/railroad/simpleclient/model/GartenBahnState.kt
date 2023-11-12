package de.ba.railroad.simpleclient.model

import model.Locomotive


data class GartenBahnState(
    val currentLocomotiveName: String = "",
    val currentSpeed: Int = 0,

)
