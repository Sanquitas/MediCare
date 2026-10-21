package com.example.medicare.presentation.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medicare.data.remote.ProfileRepository
import com.example.medicare.domain.models.UserPatient
import com.example.medicare.domain.usecase.ObtenerPerfilUseCase

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()
    private val obtenerPerfilUseCase = ObtenerPerfilUseCase(repository)

    var patientData = mutableStateOf(UserPatient())
        private set

    var isLoading = mutableStateOf(true)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        isLoading.value = true
        errorMessage.value = null

        obtenerPerfilUseCase(
            onSuccess = { user ->
                patientData.value = user
                isLoading.value = false
            },
            onError = { error ->
                errorMessage.value = error
                isLoading.value = false
            }
        )
    }

    fun cerrarSesion(onLogoutComplete: () -> Unit) {
        repository.cerrarSesion()
        onLogoutComplete() // Para navegar al Login
    }
}