package com.example.medicare.presentation.admin.monitoreo

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.domain.models.Medicamento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PacienteMonitoreo(
    val pacienteId: String,
    val nombrePaciente: String,
    val medicamentos: List<Medicamento>
)

class MonitoreoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var pacientesMonitoreo = mutableStateOf<List<PacienteMonitoreo>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        obtenerMonitoreoGlobal()
    }

    fun obtenerMonitoreoGlobal() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val adminId = auth.currentUser?.uid
                var modoDiscretoActivo = false
                var asignados = listOf<String>() // NUEVO

                if (adminId != null) {
                    val adminDoc = db.collection("usuarios").document(adminId).get().await()
                    modoDiscretoActivo = adminDoc.getBoolean("modoDiscreto") ?: false
                    // NUEVO: LEER ASIGNADOS
                    asignados = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()
                }

                // NUEVO: SI NO HAY ASIGNADOS, NO DESCARGAR NADA MÁS
                if (asignados.isEmpty()) {
                    pacientesMonitoreo.value = emptyList()
                    isLoading.value = false
                    return@launch
                }

                val usuariosSnapshot = db.collection("usuarios").get().await()
                val mapaNombres = usuariosSnapshot.documents
                    .filter { asignados.contains(it.id) } // NUEVO: FILTRAMOS USUARIOS
                    .associate { doc ->
                        var nombre = doc.getString("nombre") ?: "Paciente Desconocido"

                        if (modoDiscretoActivo && nombre != "Paciente Desconocido") {
                            val partes = nombre.trim().split(" ")
                            if (partes.size > 1) {
                                nombre = "${partes[0]} ***"
                            }
                        }

                        doc.id to nombre
                    }

                val medicamentosSnapshot = db.collectionGroup("medicamentos").get().await()

                val medicamentosAgrupados = mutableMapOf<String, MutableList<Medicamento>>()

                for (doc in medicamentosSnapshot.documents) {
                    val med = doc.toObject(Medicamento::class.java)
                    if (med != null) {
                        val userId = doc.reference.parent.parent?.id

                        // NUEVO: COMPROBAR mapaNombres.containsKey(userId) PARA DESCARTAR LOS NO ASIGNADOS
                        if (userId != null && mapaNombres.containsKey(userId)) {
                            med.id = doc.id

                            if (!medicamentosAgrupados.containsKey(userId)) {
                                medicamentosAgrupados[userId] = mutableListOf()
                            }
                            medicamentosAgrupados[userId]?.add(med)
                        }
                    }
                }

                val listaFinal = medicamentosAgrupados.map { (userId, listaMeds) ->
                    val nombreFinal = mapaNombres[userId] ?: "Paciente Desconocido"
                    PacienteMonitoreo(
                        pacienteId = userId,
                        nombrePaciente = nombreFinal,
                        medicamentos = listaMeds
                    )
                }

                pacientesMonitoreo.value = listaFinal

            } catch (e: Exception) {
                errorMessage.value = "Error al cargar monitoreo: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}