package com.example.medicare.domain.usecase

import com.example.medicare.data.remote.PatientRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarMedicamentoUseCase(private val repository: PatientRepository) {

    operator fun invoke(
        nombre: String, dosis: String, hora: String, frecuencia: String, cantidadTotal: Int,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        if (nombre.isBlank() || dosis.isBlank()) {
            onError("El nombre y la dosis no pueden estar vacíos")
            return
        }

        val tiempoInicial = obtenerMilisegundosDeHora(hora)

        val nuevoMed = hashMapOf<String, Any>(
            "nombre" to nombre,
            "dosis" to dosis,
            "hora" to hora,
            "frecuencia" to frecuencia,
            "estado" to "Pendiente",
            "horaToma" to "",
            "cantidadRestante" to cantidadTotal,
            "tiempoDesbloqueo" to tiempoInicial
        )

        repository.guardarMedicamento(nuevoMed, onSuccess, onError)
    }

    private fun obtenerMilisegundosDeHora(horaStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.ROOT)
            val date = sdf.parse(horaStr.trim().uppercase(Locale.ROOT)) ?: return System.currentTimeMillis()
            val calMed = Calendar.getInstance().apply { time = date }

            val now = Calendar.getInstance()
            now.set(Calendar.HOUR_OF_DAY, calMed.get(Calendar.HOUR_OF_DAY))
            now.set(Calendar.MINUTE, calMed.get(Calendar.MINUTE))
            now.set(Calendar.SECOND, 0)
            now.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}