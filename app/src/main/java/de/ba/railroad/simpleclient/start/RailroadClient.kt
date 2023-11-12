package de.ba.railroad.simpleclient.start

import android.app.Application
import de.ba.railroad.simpleclient.SimpleClientActivity

class RailroadClient: Application() {
    companion object {
        lateinit var instance: RailroadClient
        private set
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
        SimpleClientActivity()
    }
}