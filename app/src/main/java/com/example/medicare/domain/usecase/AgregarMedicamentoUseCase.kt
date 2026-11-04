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
        val cleanHora = horaStr.trim().uppercase(Locale.ROOT)

        // Intentamos primero con formato de 12 horas (ej. 02:30 PM)
        try {
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.ROOT)
            val date = sdf12.parse(cleanHora)
            if (date != null) {
                val calMed = Calendar.getInstance().apply { time = date }
                val now = Calendar.getInstance()
                now.set(Calendar.HOUR_OF_DAY, calMed.get(Calendar.HOUR_OF_DAY))
                now.set(Calendar.MINUTE, calMed.get(Calendar.MINUTE))
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                return now.timeInMillis
            }
        } catch (_: Exception) {}

        // Si falla, intentamos con formato de 24 horas (ej. 14:30)
        try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.ROOT)
            val date = sdf24.parse(cleanHora)
            if (date != null) {
                val calMed = Calendar.getInstance().apply { time = date }
                val now = Calendar.getInstance()
                now.set(Calendar.HOUR_OF_DAY, calMed.get(Calendar.HOUR_OF_DAY))
                now.set(Calendar.MINUTE, calMed.get(Calendar.MINUTE))
                now.set(Calendar.SECOND, 0)
                now.set(Calendar.MILLISECOND, 0)
                return now.timeInMillis
            }
        } catch (_: Exception) {}

        // Fallback seguro si ninguno coincide
        return System.currentTimeMillis()
    }
}