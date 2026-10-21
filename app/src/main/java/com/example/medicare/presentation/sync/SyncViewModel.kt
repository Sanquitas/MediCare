package com.example.medicare.presentation.sync

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.data.local.MedicareDatabase
import com.example.medicare.data.local.entities.MedicamentoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val medicamentoDao = MedicareDatabase.getDatabase(application).medicamentoDao()

    var pendientesDeSincronizar = mutableStateOf<List<MedicamentoEntity>>(emptyList())
        private set

    var isSyncing = mutableStateOf(false)
        private set

    var syncMessage = mutableStateOf("Buscando datos sin conexión...")
        private set

    init {
        verificarPendientes()
    }

    fun verificarPendientes() {
        viewModelScope.launch(Dispatchers.IO) {
            val pendientes = medicamentoDao.getUnsyncedMedicamentos()
            launch(Dispatchers.Main) {
                pendientesDeSincronizar.value = pendientes
                if (pendientes.isEmpty()) {
                    syncMessage.value = "Todo está respaldado en la nube"
                } else {
                    syncMessage.value = "Tienes ${pendientes.size} registros pendientes de subir"
                }
            }
        }
    }

    fun sincronizarDatos() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            syncMessage.value = "Error: Inicia sesión para sincronizar"
            return
        }

        val pendientes = pendientesDeSincronizar.value
        if (pendientes.isEmpty()) return

        isSyncing.value = true
        syncMessage.value = "Sincronizando ${pendientes.size} registros..."

        viewModelScope.launch(Dispatchers.IO) {
            var subidosConExito = 0

            for (med in pendientes) {
                try {
                    val mapaFirebase = hashMapOf(
                        "nombre" to med.nombre,
                        "dosis" to med.dosis,
                        "hora" to med.hora,
                        "frecuencia" to med.frecuencia,
                        "estado" to med.estado,
                        "horaToma" to med.horaToma,
                        "cantidadRestante" to med.cantidadRestante.toString(),
                        "tiempoDesbloqueo" to med.tiempoDesbloqueo.toString()
                    )

                    db.collection("usuarios").document(userId)
                        .collection("medicamentos").document(med.id)
                        .set(mapaFirebase).await()

                    medicamentoDao.markAsSynced(med.id)
                    subidosConExito++

                } catch (e: Exception) {
                    android.util.Log.e("SYNC", "Error subiendo ${med.nombre}: ${e.message}")
                }
            }

            launch(Dispatchers.Main) {
                isSyncing.value = false
                if (subidosConExito == pendientes.size) {
                    syncMessage.value = "¡Sincronización completada con éxito!"
                } else {
                    syncMessage.value = "Se subieron $subidosConExito de ${pendientes.size}. Revisa tu conexión."
                }
                verificarPendientes()
            }
        }
    }
}