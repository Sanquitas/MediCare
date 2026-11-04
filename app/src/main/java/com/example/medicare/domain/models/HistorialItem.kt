package com.example.medicare.domain.models // Usa el paquete correcto según tu carpeta

data class HistorialItem(
    val id: String,
    val nombrePaciente: String,
    val nombreMedicamento: String,
    val horaTomaReal: String,
    val estado: String
)