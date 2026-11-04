package com.example.medicare.presentation.admin.historial

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.domain.models.HistorialItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminHistorialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var historial = mutableStateOf<List<HistorialItem>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    init {
        obtenerHistorial()
    }

    fun obtenerHistorial() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val adminId = auth.currentUser?.uid
                var modoDiscretoActivo = false
                var asignados = listOf<String>()

                if (adminId != null) {
                    val adminDoc = db.collection("usuarios").document(adminId).get().await()
                    modoDiscretoActivo = adminDoc.getBoolean("modoDiscreto") ?: false
                    asignados = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()
                }

                if (asignados.isEmpty()) {
                    historial.value = emptyList()
                    isLoading.value = false
                    return@launch
                }

                val historialTemp = mutableListOf<HistorialItem>()
                val usuariosSnapshot = db.collection("usuarios").whereEqualTo("rol", "Paciente").get().await()

                for (usuarioDoc in usuariosSnapshot.documents) {
                    if (!asignados.contains(usuarioDoc.id)) {
                        continue
                    }

                    val nombreOriginal = usuarioDoc.getString("nombre") ?: "Desconocido"
                    var nombrePaciente = nombreOriginal

                    if (modoDiscretoActivo && nombreOriginal != "Desconocido") {
                        val partes = nombreOriginal.trim().split("\\s+".toRegex())
                        nombrePaciente = if (partes.size > 1) {
                            "${partes[0]} ***"
                        } else {
                            "${partes[0]} ***"
                        }
                    }

                    val medSnapshot = db.collection("usuarios").document(usuarioDoc.id)
                        .collection("medicamentos").whereIn("estado", listOf("Tomado", "Omitido")).get().await()

                    for (medDoc in medSnapshot.documents) {
                        val nombreMed = medDoc.getString("nombre") ?: ""
                        val horaToma = medDoc.getString("horaToma") ?: ""
                        val estado = medDoc.getString("estado") ?: ""

                        historialTemp.add(
                            HistorialItem(
                                id = medDoc.id,
                                nombrePaciente = nombrePaciente,
                                nombreMedicamento = nombreMed,
                                horaTomaReal = if (estado == "Tomado") horaToma else "Omitido",
                                estado = estado
                            )
                        )
                    }
                }

                historial.value = emptyList()
                historial.value = historialTemp

            } catch (e: Exception) {
                android.util.Log.e("MEDICARE", "Error historial: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }
}