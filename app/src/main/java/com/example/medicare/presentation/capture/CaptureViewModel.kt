package com.example.medicare.presentation.capture

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CaptureViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun confirmarToma(
        medicamentoId: String,
        fotoBitmap: android.graphics.Bitmap?,
        ubicacion: String,
        onSuccess: () -> Unit
    ) {
        if (fotoBitmap == null || ubicacion.isBlank() || ubicacion.contains("Buscando")) {
            errorMessage.value = "Evidencia incompleta."
            return
        }

        val userId = auth.currentUser?.uid ?: return
        isLoading.value = true

        val docRef = db.collection("usuarios").document(userId).collection("medicamentos").document(medicamentoId)

        docRef.get().addOnSuccessListener { snapshot ->
            val med = snapshot.toObject(com.example.medicare.domain.models.Medicamento::class.java)
            if (med != null) {
                // Obtenemos la hora real en la que se tomó la foto
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
                val horaRealToma = sdf.format(java.util.Calendar.getInstance().time)

                // 1. Cerramos la actual como Tomada
                docRef.update(mapOf("estado" to "Tomado", "horaToma" to horaRealToma))

                // 2. Si quedan pastillas, agendamos la siguiente
                if (med.cantidadRestante > 1) {
                    val sdfCalc = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
                    val dateObj = sdfCalc.parse(med.hora.trim().uppercase(java.util.Locale.ROOT))
                    val cal = java.util.Calendar.getInstance()
                    if (dateObj != null) cal.time = dateObj

                    val horasAIncrementar = when (med.frecuencia) {
                        "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                        "Cada 12 horas" -> 12; else -> 24
                    }
                    cal.add(java.util.Calendar.HOUR_OF_DAY, horasAIncrementar)

                    // COOLDOWN ABSOLUTO:
                    val cooldownMillis = horasAIncrementar * 60 * 60 * 1000L
                    val nextTiempoDesbloqueo = System.currentTimeMillis() + cooldownMillis

                    val nextMed = hashMapOf(
                        "nombre" to med.nombre, "dosis" to med.dosis,
                        "hora" to sdfCalc.format(cal.time), "frecuencia" to med.frecuencia,
                        "estado" to "Pendiente", "horaToma" to "",
                        "cantidadRestante" to (med.cantidadRestante - 1),
                        "tiempoDesbloqueo" to nextTiempoDesbloqueo // <-- CANDADO ACTIVO
                    )
                    db.collection("usuarios").document(userId).collection("medicamentos").add(nextMed)
                }
                // ...

                isLoading.value = false
                onSuccess()
            }
        }.addOnFailureListener {
            isLoading.value = false
            errorMessage.value = "Error al actualizar."
        }
    }
}