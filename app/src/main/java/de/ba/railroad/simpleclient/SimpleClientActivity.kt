package de.ba.railroad.simpleclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import de.ba.railroad.simpleclient.model.GartenbahnRepository
import de.ba.railroad.simpleclient.model.ServerData
import de.ba.railroad.simpleclient.ui.ViewModelFac
import de.ba.railroad.simpleclient.ui.mainScreen

class SimpleClientActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository: ServerData = GartenbahnRepository(lifecycleScope)
        val factory = ViewModelFac(repository)
        setContent {
            MaterialTheme {
                mainScreen(factory)
            }
        }
    }
}




