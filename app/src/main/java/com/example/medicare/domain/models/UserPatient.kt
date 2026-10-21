package com.example.medicare.domain.models

data class UserPatient(
    val nombre: String = "",
    val email: String = "",
    val tipoSangre: String = "N/A",
    val edad: String = "N/A",
    val contactoEmergencia: String = "N/A",
    val rol: String = ""
)