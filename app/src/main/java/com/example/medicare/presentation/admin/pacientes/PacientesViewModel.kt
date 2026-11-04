package com.example.medicare.presentation.admin.pacientes

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.domain.models.UserPatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // IMPORTACIÓN NECESARIA PARA GUARDAR
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PacientesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var pacientesOriginales = listOf<UserPatient>()

    var pacientes = mutableStateOf<List<UserPatient>>(emptyList())
        private set

    // --- NUEVAS VARIABLES PARA EL BOTÓN '+' ---
    var todosLosPacientes = mutableStateOf<List<UserPatient>>(emptyList())
        private set

    var pacientesAsignadosIds = mutableStateOf<List<String>>(emptyList())
        private set
    // ------------------------------------------

    var searchQuery = mutableStateOf("")
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        obtenerPacientes()
    }

    fun obtenerPacientes() {
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

                pacientesAsignadosIds.value = asignados // NUEVO

                val querySnapshot = db.collection("usuarios")
                    .whereEqualTo("rol", "Paciente")
                    .get().await()

                val listaPacientes = querySnapshot.documents.mapNotNull { documento ->
                    val paciente = documento.toObject(UserPatient::class.java)
                    paciente?.id = documento.id

                    if (modoDiscretoActivo && paciente != null && paciente.nombre.isNotBlank()) {
                        val partes = paciente.nombre.trim().split(" ")
                        if (partes.size > 1) {
                            paciente.nombre = "${partes[0]} ***"
                        }
                    }

                    paciente
                }.filter { it.nombre.isNotBlank() }

                todosLosPacientes.value = listaPacientes // NUEVO: Todos los pacientes para el modal

                // NUEVO: FILTRAMOS LOS QUE VAMOS A MOSTRAR EN LA LISTA PRINCIPAL
                val listaFiltrada = listaPacientes.filter { asignados.contains(it.id) }

                pacientesOriginales = listaFiltrada // CAMBIADO PARA USAR LA LISTA FILTRADA
                aplicarFiltro(searchQuery.value)

            } catch (e: Exception) {
                errorMessage.value = "Error al obtener la lista: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
        aplicarFiltro(newQuery)
    }

    private fun aplicarFiltro(query: String) {
        if (query.isBlank()) {
            pacientes.value = pacientesOriginales
        } else {
            pacientes.value = pacientesOriginales.filter { paciente ->
                paciente.nombre.contains(query, ignoreCase = true)
            }
        }
    }

    fun guardarAsignaciones(nuevosIds: List<String>) {
        val adminId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("usuarios").document(adminId)
                    .set(mapOf("pacientesAsignados" to nuevosIds), SetOptions.merge())
                    .await()
                obtenerPacientes()
            } catch (e: Exception) {
                android.util.Log.e("MEDICARE", "Error al guardar asignaciones: ${e.message}")
            }
        }
    }
}