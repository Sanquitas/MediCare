package com.example.medicare.presentation.admin.pacientes

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.domain.models.UserPatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class PacientesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var pacientesOriginales = listOf<UserPatient>()

    var pacientes = mutableStateOf<List<UserPatient>>(emptyList())
        private set

    var todosLosPacientes = mutableStateOf<List<UserPatient>>(emptyList())
        private set

    var pacientesAsignadosIds = mutableStateOf<List<String>>(emptyList())
        private set

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
                var asignados = listOf<String>()

                if (adminId != null) {
                    val adminDoc = db.collection("usuarios").document(adminId).get().await()
                    modoDiscretoActivo = adminDoc.getBoolean("modoDiscreto") ?: false
                    asignados = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()
                }

                pacientesAsignadosIds.value = asignados

                val querySnapshot = db.collection("usuarios")
                    .whereIn("rol", listOf("Paciente", "paciente"))
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

                todosLosPacientes.value = listaPacientes

                val listaFiltrada = listaPacientes.filter { asignados.contains(it.id) }

                pacientesOriginales = listaFiltrada
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

    fun agregarMedicamentoAPaciente(
        pacienteId: String,
        nombre: String,
        dosis: String,
        hora: String,
        frecuencia: String,
        cantidadTotal: Int,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val nombreLimpio = nombre.trim()

                val medsSnapshot = db.collection("usuarios")
                    .document(pacienteId)
                    .collection("medicamentos")
                    .get()
                    .await()

                val yaExiste = medsSnapshot.documents.any { doc ->
                    val medNombre = doc.getString("nombre") ?: ""
                    medNombre.trim().equals(nombreLimpio, ignoreCase = true)
                }

                if (yaExiste) {
                    onError("El paciente ya tiene registrado un medicamento con el nombre '$nombreLimpio'.")
                    return@launch
                }

                val tiempoInicial = obtenerMilisegundosDeHora(hora)
                val medicamentoId = UUID.randomUUID().toString()

                val nuevoMed = hashMapOf<String, Any>(
                    "nombre" to nombreLimpio,
                    "dosis" to dosis.trim(),
                    "hora" to hora.trim().uppercase(),
                    "frecuencia" to frecuencia,
                    "estado" to "Pendiente",
                    "horaToma" to "",
                    "cantidadRestante" to cantidadTotal,
                    "tiempoDesbloqueo" to tiempoInicial
                )

                db.collection("usuarios").document(pacienteId)
                    .collection("medicamentos").document(medicamentoId)
                    .set(nuevoMed)
                    .await()

                obtenerPacientes()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al agregar medicamento")
            }
        }
    }

    private fun obtenerMilisegundosDeHora(horaStr: String): Long {
        val cleanHora = horaStr.trim().uppercase(Locale.ROOT)
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

        return System.currentTimeMillis()
    }
}
