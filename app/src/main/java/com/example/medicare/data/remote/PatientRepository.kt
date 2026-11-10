package com.example.medicare.data.remote

import android.content.Context
import com.example.medicare.data.local.dao.MedicamentoDao
import com.example.medicare.data.local.entities.MedicamentoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class PatientRepository(private val medicamentoDao: MedicamentoDao, private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun guardarMedicamento(
        nuevoMed: HashMap<String, Any>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Error: No hay usuario logueado.")
            return
        }

        val medicamentoId = UUID.randomUUID().toString()

        val entidadLocal = MedicamentoEntity(
            id = medicamentoId,
            nombre = nuevoMed["nombre"].toString(),
            dosis = nuevoMed["dosis"].toString(),
            hora = nuevoMed["hora"].toString(),
            frecuencia = nuevoMed["frecuencia"].toString(),
            estado = nuevoMed["estado"].toString(),
            horaToma = nuevoMed["horaToma"].toString(),
            cantidadRestante = nuevoMed["cantidadRestante"].toString().toIntOrNull() ?: 0,
            tiempoDesbloqueo = nuevoMed["tiempoDesbloqueo"].toString().toLongOrNull() ?: 0L,
            isSynced = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                medicamentoDao.insertMedicamento(entidadLocal)

                // ---- CÓDIGO NUEVO (PASO 4: ALARMA DE LA PRIMERA TOMA) ----
                val tiempo = nuevoMed["tiempoDesbloqueo"].toString().toLongOrNull() ?: 0L
                val scheduler = com.example.medicare.presentation.patient.notifications.AlarmScheduler(context)
                scheduler.programarAlarma(
                    id = medicamentoId,
                    nombre = nuevoMed["nombre"].toString(),
                    dosis = nuevoMed["dosis"].toString(),
                    tiempoMilisegundos = tiempo
                )
                // -----------------------------------------------------------

                launch(Dispatchers.Main) { onSuccess() }

                db.collection("usuarios").document(userId).collection("medicamentos")
                    .document(medicamentoId)
                    .set(nuevoMed)
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            medicamentoDao.markAsSynced(medicamentoId)
                        }
                    }
                    .addOnFailureListener {
                    }

            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError("Fallo local: ${e.message}") }
            }
        }
    }
}