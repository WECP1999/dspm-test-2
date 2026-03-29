package com.udb.examenparcial2.ui.catalog

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.udb.examenparcial2.data.model.Destination
import com.udb.examenparcial2.data.repository.DestinationRepository
import com.udb.examenparcial2.util.Resource
import kotlinx.coroutines.launch

class DestinationViewModel(private val repository: DestinationRepository = DestinationRepository()) : ViewModel() {

    val destinations: LiveData<Resource<List<Destination>>> = repository.getDestinations().asLiveData()

    private val _operationStatus = MutableLiveData<Resource<Unit>>()
    val operationStatus: LiveData<Resource<Unit>> = _operationStatus

    fun saveDestination(destination: Destination, imageUri: String?, isEdit: Boolean) {
        if (!validate(destination, imageUri, isEdit)) return

        _operationStatus.value = Resource.Loading()
        viewModelScope.launch {
            val result = if (isEdit) {
                Log.d("DestinationViewModel", "Updating destination: ${destination.id}")
                repository.updateDestination(destination, imageUri)
            } else {
                Log.d("DestinationViewModel", "Creating new destination")
                repository.createDestination(destination, imageUri!!)
            }
            _operationStatus.value = result
        }
    }

    fun deleteDestination(id: String) {
        _operationStatus.value = Resource.Loading()
        viewModelScope.launch {
            _operationStatus.value = repository.deleteDestination(id)
        }
    }

    private fun validate(destination: Destination, imageUri: String?, isEdit: Boolean): Boolean {
        if (destination.name.isBlank() || destination.country == "Select a country" || 
            destination.description.isBlank()) {
            _operationStatus.value = Resource.Error("All fields are required")
            return false
        }
        if (destination.price <= 0) {
            _operationStatus.value = Resource.Error("Price must be greater than 0")
            return false
        }
        if (destination.description.length < 20) {
            _operationStatus.value = Resource.Error("Description must be at least 20 characters")
            return false
        }
        if (!isEdit && imageUri == null) {
            _operationStatus.value = Resource.Error("An image must be selected")
            return false
        }
        return true
    }
}
