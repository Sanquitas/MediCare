package com.example.medicare.domain.models

data class Medicamento(
    var id: String = "",
    val nombre: String = "",
    val dosis: String = "",
    val hora: String = "",
    val frecuencia: String = "",
    val estado: String = "Pendiente",
    val horaToma: String = "",
    val cantidadRestante: Int = 1,
    val tiempoDesbloqueo: Long = 0L,
    val fotoEvidenciaBase64: String = ""
)