package de.ba.railroad.simpleclient.ui

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.ba.railroad.simpleclient.model.GartenBahnState
import de.ba.railroad.simpleclient.model.ServerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GartenbahnViewModel(private val repository: ServerData) : ViewModel() {
    //Gartenbahn state
    private val _uiState = MutableStateFlow(GartenBahnState())
    private val repoData = repository.getServerList()
    val uiState: StateFlow<GartenBahnState> = _uiState.asStateFlow()

    fun changeLocomotive(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                currentLocomotiveName = name
            )
        }
        repoData.value?.forEach { server ->
            if(server.name == name){
                viewModelScope.launch(Dispatchers.IO) {
                    repository.changeLocomotive(server.url)
                }
            }
        }
    }
    fun changeSpeed(speed: Int, direction: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.postSpeed(speed, direction)
        }
    }
}