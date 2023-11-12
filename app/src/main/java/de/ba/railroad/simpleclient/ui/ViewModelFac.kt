package de.ba.railroad.simpleclient.ui

import de.ba.railroad.simpleclient.model.ServerData


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class ViewModelFac(private val repository: ServerData) : NewInstanceFactory(){
     override fun <T : ViewModel> create(modelClass: Class<T>): T = GartenbahnViewModel(repository) as T

}
