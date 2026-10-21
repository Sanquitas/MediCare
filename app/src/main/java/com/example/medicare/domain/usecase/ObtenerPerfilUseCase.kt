package com.example.medicare.domain.usecase

import com.example.medicare.data.remote.ProfileRepository
import com.example.medicare.domain.models.UserPatient

class ObtenerPerfilUseCase(private val repository: ProfileRepository) {

    operator fun invoke(onSuccess: (UserPatient) -> Unit, onError: (String) -> Unit) {
        repository.obtenerPerfilUsuario(onSuccess, onError)
    }
}