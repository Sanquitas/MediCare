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
import com.google.firebase.firestore.SetOptions

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val medicamentoDao = MedicareDatabase.getDatabase(application).medicamentoDao()

    // Estados de Room
    var pendientesDeSincronizar = mutableStateOf<List<MedicamentoEntity>>(emptyList())
        private set
    var pendingImagesCount = mutableStateOf(0)
    var uploadedImagesCount = mutableStateOf(0) // Ajustado a 0 inicial

    // Estados de Red y Sincronización
    var isSyncing = mutableStateOf(false)
        private set
    var networkStatus = mutableStateOf("Conectado (Wi-Fi)")
    var networkSubtext = mutableStateOf("Modo Online: Datos listos para respaldar en la nube")
    var lastSyncTime = mutableStateOf("Hace un momento")
    var syncProgress = mutableStateOf(1.0f) // 1.0f = 100%

    // Configuraciones de los Switches
    var isWifiOnlyEnabled = mutableStateOf(true)
    var isBackgroundSyncEnabled = mutableStateOf(true)

    init {
        verificarPendientes()
    }

    fun verificarPendientes() {
        viewModelScope.launch(Dispatchers.IO) {
            val todosLosMedicamentos = medicamentoDao.getAllMedicamentos()
            val pendientes = medicamentoDao.getUnsyncedMedicamentos()

            launch(Dispatchers.Main) {
                pendientesDeSincronizar.value = pendientes

                // Cálculo matemático real del progreso
                if (todosLosMedicamentos.isEmpty()) {
                    syncProgress.value = 1.0f
                } else {
                    val sincronizados = todosLosMedicamentos.size - pendientes.size
                    syncProgress.value = sincronizados.toFloat() / todosLosMedicamentos.size.toFloat()
                }
            }
        }
    }

    fun toggleWifiOnly(enabled: Boolean) {
        isWifiOnlyEnabled.value = enabled
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        isBackgroundSyncEnabled.value = enabled
    }

    fun sincronizarDatos() {
        val userId = auth.currentUser?.uid ?: return
        val pendientes = pendientesDeSincronizar.value

        if (pendientes.isEmpty() && pendingImagesCount.value == 0) return

        isSyncing.value = true
        // Reiniciamos el progreso visual solo para los elementos que se van a subir
        syncProgress.value = 0.0f

        viewModelScope.launch(Dispatchers.IO) {
            var subidosConExito = 0
            val totalItems = pendientes.size

            for ((index, med) in pendientes.withIndex()) {
                try {
                    // ¡CORREGIDO! Ya no usamos .toString() en los números
                    val mapaFirebase = hashMapOf(
                        "nombre" to med.nombre,
                        "dosis" to med.dosis,
                        "hora" to med.hora,
                        "frecuencia" to med.frecuencia,
                        "estado" to med.estado,
                        "horaToma" to med.horaToma,
                        "cantidadRestante" to med.cantidadRestante,
                        "tiempoDesbloqueo" to med.tiempoDesbloqueo,
                        "fechaRegistro" to med.fechaRegistro
                    )

                    db.collection("usuarios").document(userId)
                        .collection("medicamentos").document(med.id)
                        .set(mapaFirebase, SetOptions.merge()).await()

                    medicamentoDao.markAsSynced(med.id)
                    subidosConExito++

                    // Actualizar progreso visual mientras sube
                    launch(Dispatchers.Main) {
                        syncProgress.value = (subidosConExito.toFloat() / totalItems.toFloat())
                    }

                } catch (e: Exception) {
                    android.util.Log.e("SYNC", "Error subiendo ${med.nombre}: ${e.message}")
                }
            }

            launch(Dispatchers.Main) {
                isSyncing.value = false
                lastSyncTime.value = "Justo ahora"
                verificarPendientes()
            }
        }
    }
}