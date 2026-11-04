package com.example.medicare.domain.models

data class UserPatient(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var tipoSangre: String = "N/A",
    var edad: String = "N/A",
    var contactoEmergencia: String = "N/A",
    var rol: String = ""
)