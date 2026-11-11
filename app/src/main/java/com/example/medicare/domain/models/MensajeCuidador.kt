package com.example.medicare.domain.models

// Mensaje que el cuidador (admin) envía al paciente. Se guarda en usuarios/{pacienteId}/mensajes
data class MensajeCuidador(
    var id: String = "",
    val texto: String = "",
    val remitenteId: String = "",
    val remitenteNombre: String = "",
    val fecha: Long = 0L,
    val leido: Boolean = false
)
