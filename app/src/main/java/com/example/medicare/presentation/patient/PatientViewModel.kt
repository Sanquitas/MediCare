package com.example.medicare.presentation.patient

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.medicare.domain.models.Medicamento
import com.example.medicare.domain.models.UserPatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.medicare.data.local.MedicareDatabase
import com.example.medicare.data.remote.PatientRepository
import com.example.medicare.domain.usecase.AgregarMedicamentoUseCase

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val medicamentoDao = MedicareDatabase.getDatabase(application).medicamentoDao()

    private val repository = PatientRepository(medicamentoDao)
    private val agregarMedicamentoUseCase = AgregarMedicamentoUseCase(repository)

    var patientData = mutableStateOf(UserPatient())
        private set

    var medicamentosDelDia = mutableStateListOf<Medicamento>()
        private set

    var isLoading = mutableStateOf(true)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        cargarDatosDelPaciente()
    }

    private fun cargarDatosDelPaciente() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage.value = "Usuario no autenticado"
            isLoading.value = false
            return
        }

        isLoading.value = true
        errorMessage.value = null

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    patientData.value = UserPatient(
                        nombre = document.getString("nombre") ?: "Paciente",
                        email = document.getString("email") ?: auth.currentUser?.email ?: "",
                        tipoSangre = document.getString("tipoSangre") ?: "N/A",
                        edad = document.getString("edad") ?: "N/A",
                        contactoEmergencia = document.getString("contactoEmergencia") ?: "N/A"
                    )
                }
                cargarMedicamentos(userId)
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                errorMessage.value = "Error al cargar perfil: ${e.localizedMessage}"
            }
    }

    private fun cargarMedicamentos(userId: String) {
        db.collection("usuarios").document(userId).collection("medicamentos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading.value = false
                    errorMessage.value = "Error al cargar medicamentos: ${error.localizedMessage}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    medicamentosDelDia.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val med = doc.toObject(Medicamento::class.java)
                            if (med != null) {
                                medicamentosDelDia.add(med.copy(id = doc.id))
                            }
                        } catch (e: Exception) {
                            // Ignorar documentos corruptos
                        }
                    }
                    isLoading.value = false
                }
            }
    }

    fun agregarMedicamento(
        nombre: String,
        dosis: String,
        hora: String,
        frecuencia: String,
        cantidadTotal: Int
    ) {
        agregarMedicamentoUseCase(
            nombre = nombre,
            dosis = dosis,
            hora = hora,
            frecuencia = frecuencia,
            cantidadTotal = cantidadTotal,
            onSuccess = {
                android.util.Log.d("MEDICARE", "¡Medicamento guardado localmente y en nube!")
            },
            onError = { error ->
                errorMessage.value = error
                android.util.Log.e("MEDICARE", error)
            }
        )
    }

    fun esHoraDeTomar(horaStr: String): Boolean {
        return try {
            val cleanHora = horaStr.trim().uppercase(Locale.ROOT)
            val sdf = SimpleDateFormat("hh:mm a", Locale.ROOT)
            val date = sdf.parse(cleanHora) ?: return false

            val calMed = Calendar.getInstance().apply { time = date }
            val now = Calendar.getInstance()

            val hourNow = now.get(Calendar.HOUR_OF_DAY)
            val minNow = now.get(Calendar.MINUTE)

            val hourMed = calMed.get(Calendar.HOUR_OF_DAY)
            val minMed = calMed.get(Calendar.MINUTE)

            (hourNow > hourMed) || (hourNow == hourMed && minNow >= minMed)
        } catch (e: Exception) {
            true
        }
    }

    fun confirmarToma(
        medicamentoId: String,
        fotoBitmap: Any?,
        latitud: String?,
        longitud: String?,
        onSuccess: () -> Unit
    ) {
        if (fotoBitmap == null || latitud.isNullOrBlank() || longitud.isNullOrBlank()) {
            errorMessage.value = "Debes tomar la foto y obtener tu ubicación GPS antes de guardar."
            return
        }

        isLoading.value = true
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                medicamentoDao.updateEstado(medicamentoId, "Tomado")

                launch(Dispatchers.Main) {
                    isLoading.value = false
                    onSuccess()
                }

                // 2. INTENTO EN LA NUBE (Firebase)
                db.collection("usuarios").document(userId)
                    .collection("medicamentos").document(medicamentoId)
                    .update("estado", "Tomado")
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            medicamentoDao.markAsSynced(
                                medicamentoId
                            )
                        }
                    }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Error local: ${e.message}"
                }
            }
        }
    }

    private fun calcularProximaHora(horaActual: String, frecuencia: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
            val date =
                sdf.parse(horaActual.trim().uppercase(java.util.Locale.ROOT)) ?: return horaActual
            val cal = java.util.Calendar.getInstance().apply { time = date }

            val horasAIncrementar = when (frecuencia) {
                "Cada 4 horas" -> 4
                "Cada 6 horas" -> 6
                "Cada 8 horas" -> 8
                "Cada 12 horas" -> 12
                "Cada 24 horas (Una vez al día)" -> 24
                else -> 24
            }
            cal.add(java.util.Calendar.HOUR_OF_DAY, horasAIncrementar)
            sdf.format(cal.time)
        } catch (e: Exception) {
            horaActual
        }
    }

    fun omitirMedicamento(medicamentoId: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Buscamos los datos de la pastilla en el celular, no en Firebase
            val medLocal = medicamentoDao.getMedicamentoById(medicamentoId) ?: return@launch

            // 2. Actualizamos la pastilla actual a "Omitido"
            medicamentoDao.updateEstado(medicamentoId, "Omitido")

            // Intentamos actualizar la nube
            db.collection("usuarios").document(userId).collection("medicamentos")
                .document(medicamentoId)
                .update("estado", "Omitido")
                .addOnSuccessListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        medicamentoDao.markAsSynced(
                            medicamentoId
                        )
                    }
                }

            // 3. Generamos la siguiente toma (Si aún quedan pastillas)
            if (medLocal.cantidadRestante > 1) {
                val nextHora = calcularProximaHora(medLocal.hora, medLocal.frecuencia)
                val horasAIncrementar = when (medLocal.frecuencia) {
                    "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                    "Cada 12 horas" -> 12; else -> 24
                }
                val cooldownMillis = horasAIncrementar * 60 * 60 * 1000L
                val nextTiempoDesbloqueo = System.currentTimeMillis() + cooldownMillis
                val nuevoId = UUID.randomUUID().toString()

                // A) Guardar nueva toma en Room
                val nextMedLocal = com.example.medicare.data.local.entities.MedicamentoEntity(
                    id = nuevoId,
                    nombre = medLocal.nombre,
                    dosis = medLocal.dosis,
                    hora = nextHora,
                    frecuencia = medLocal.frecuencia,
                    estado = "Pendiente",
                    horaToma = "",
                    cantidadRestante = medLocal.cantidadRestante - 1,
                    tiempoDesbloqueo = nextTiempoDesbloqueo,
                    isSynced = false
                )
                medicamentoDao.insertMedicamento(nextMedLocal)

                // B) Intentar subir nueva toma a Firebase
                val nextMedFirebase = hashMapOf(
                    "nombre" to medLocal.nombre, "dosis" to medLocal.dosis,
                    "hora" to nextHora, "frecuencia" to medLocal.frecuencia,
                    "estado" to "Pendiente", "horaToma" to "",
                    "cantidadRestante" to (medLocal.cantidadRestante - 1).toString(),
                    "tiempoDesbloqueo" to nextTiempoDesbloqueo.toString()
                )
                db.collection("usuarios").document(userId).collection("medicamentos")
                    .document(nuevoId).set(nextMedFirebase)
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) { medicamentoDao.markAsSynced(nuevoId) }
                    }
            }
        }
    }
}