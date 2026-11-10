package com.example.medicare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos_locales")
data class MedicamentoEntity(
    @PrimaryKey val id: String, // Usaremos un ID generado localmente
    val nombre: String,
    val dosis: String,
    val hora: String,
    val frecuencia: String,
    val estado: String,
    val horaToma: String,
    val cantidadRestante: Int,
    val tiempoDesbloqueo: Long,
    val fotoLocalUri: String? = null,
    val isSynced: Boolean = false,
    val fechaRegistro: Long = 0L
)