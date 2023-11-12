package de.ba.railroad.simpleclient.model

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LiveData
import model.Server

interface ServerData {
    fun getServerList(): LiveData<List<Server>>
    suspend fun changeLocomotive(url: String)
    suspend fun postSpeed(speed: Int, direction: Int)
}