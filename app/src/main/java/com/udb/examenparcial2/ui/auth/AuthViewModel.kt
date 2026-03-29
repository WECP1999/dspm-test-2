package com.udb.examenparcial2.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udb.examenparcial2.data.repository.AuthRepository
import com.udb.examenparcial2.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authStatus = MutableLiveData<Resource<FirebaseUser>>()
    val authStatus: LiveData<Resource<FirebaseUser>> = _authStatus

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authStatus.value = Resource.Error("Fields cannot be empty")
            return
        }
        _authStatus.value = Resource.Loading()
        viewModelScope.launch {
            _authStatus.value = repository.login(email, password)
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authStatus.value = Resource.Error("Fields cannot be empty")
            return
        }
        _authStatus.value = Resource.Loading()
        viewModelScope.launch {
            _authStatus.value = repository.register(email, password)
        }
    }

    fun isUserLoggedIn() = repository.isUserLoggedIn()

    fun logout() = repository.logout()
}
