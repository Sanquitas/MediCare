package com.example.medicare.presentation.patient

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.data.local.MedicareDatabase
import com.example.medicare.data.remote.PatientRepository
import com.example.medicare.domain.models.Medicamento
import com.example.medicare.domain.models.UserPatient
import com.example.medicare.domain.usecase.AgregarMedicamentoUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
                cargarMedicamentosLocales()
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                errorMessage.value = "Error al cargar perfil: ${e.localizedMessage}"
            }
    }

    private fun cargarMedicamentosLocales() {
        viewModelScope.launch(Dispatchers.IO) {
            medicamentoDao.getAllMedicamentosLive().collect { listaEntidades ->
                val listaMapeada = listaEntidades.map { entidad ->
                    Medicamento(
                        id = entidad.id,
                        nombre = entidad.nombre,
                        dosis = entidad.dosis,
                        hora = entidad.hora,
                        frecuencia = entidad.frecuencia,
                        estado = entidad.estado,
                        horaToma = entidad.horaToma,
                        cantidadRestante = entidad.cantidadRestante,
                        tiempoDesbloqueo = entidad.tiempoDesbloqueo
                    )
                }

                launch(Dispatchers.Main) {
                    medicamentosDelDia.clear()
                    medicamentosDelDia.addAll(listaMapeada)
                    isLoading.value = false
                }
            }
        }
    }

    fun agregarMedicamento(nombre: String, dosis: String, hora: String, frecuencia: String, cantidadTotal: Int) {
        agregarMedicamentoUseCase(
            nombre = nombre,
            dosis = dosis,
            hora = hora,
            frecuencia = frecuencia,
            cantidadTotal = cantidadTotal,
            onSuccess = { android.util.Log.d("MEDICARE", "¡Guardado!") },
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

        // 1. Convertimos y achicamos la foto correctamente
        var fotoBase64 = ""
        try {
            val bitmapOriginal = when (fotoBitmap) {
                is Bitmap -> fotoBitmap
                is ImageBitmap -> fotoBitmap.asAndroidBitmap() // ¡ERROR CORREGIDO AQUÍ!
                else -> null
            }

            if (bitmapOriginal != null) {
                val maxResolution = 600f
                val ratio = Math.min(maxResolution / bitmapOriginal.width, maxResolution / bitmapOriginal.height)
                val width = Math.round(ratio * bitmapOriginal.width)
                val height = Math.round(ratio * bitmapOriginal.height)

                val resizedBitmap = Bitmap.createScaledBitmap(bitmapOriginal, width, height, true)
                val baos = java.io.ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                val imageBytes = baos.toByteArray()
                fotoBase64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            android.util.Log.e("MEDICARE", "Error convirtiendo imagen: ${e.message}")
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val medLocal = medicamentoDao.getMedicamentoById(medicamentoId) ?: return@launch
                val sdfHoraReal = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
                val horaTomaReal = sdfHoraReal.format(java.util.Calendar.getInstance().time)

                // 2. Actualizamos Room
                medicamentoDao.updateEstado(medicamentoId, "Tomado")

                // 3. Actualizamos Firebase
                db.collection("usuarios").document(userId)
                    .collection("medicamentos").document(medicamentoId)
                    .update(
                        mapOf(
                            "estado" to "Tomado",
                            "horaToma" to horaTomaReal,
                            "fotoEvidenciaBase64" to fotoBase64
                        )
                    )
                    .addOnSuccessListener {
                        viewModelScope.launch(Dispatchers.IO) { medicamentoDao.markAsSynced(medicamentoId) }
                    }

                // 4. GENERAMOS LA NUEVA TARJETA
                if (medLocal.cantidadRestante > 1) {
                    val nextHora = calcularProximaHora(medLocal.hora, medLocal.frecuencia)
                    val horasAIncrementar = when (medLocal.frecuencia) {
                        "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                        "Cada 12 horas" -> 12; else -> 24
                    }
                    val cooldownMillis = horasAIncrementar * 60 * 60 * 1000L
                    val nextTiempoDesbloqueo = System.currentTimeMillis() + cooldownMillis
                    val nuevoId = UUID.randomUUID().toString()

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

                    val nextMedFirebase = hashMapOf(
                        "nombre" to medLocal.nombre,
                        "dosis" to medLocal.dosis,
                        "hora" to nextHora,
                        "frecuencia" to medLocal.frecuencia,
                        "estado" to "Pendiente",
                        "horaToma" to "",
                        "cantidadRestante" to (medLocal.cantidadRestante - 1),
                        "tiempoDesbloqueo" to nextTiempoDesbloqueo
                    )
                    db.collection("usuarios").document(userId).collection("medicamentos")
                        .document(nuevoId).set(nextMedFirebase)
                }

                launch(Dispatchers.Main) {
                    isLoading.value = false
                    onSuccess()
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
            val date = sdf.parse(horaActual.trim().uppercase(java.util.Locale.ROOT)) ?: return horaActual
            val cal = java.util.Calendar.getInstance().apply { time = date }
            val horasAIncrementar = when (frecuencia) {
                "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                "Cada 12 horas" -> 12; else -> 24
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
            val medLocal = medicamentoDao.getMedicamentoById(medicamentoId) ?: return@launch
            medicamentoDao.updateEstado(medicamentoId, "Omitido")

            db.collection("usuarios").document(userId).collection("medicamentos")
                .document(medicamentoId)
                .update("estado", "Omitido")
                .addOnSuccessListener {
                    viewModelScope.launch(Dispatchers.IO) { medicamentoDao.markAsSynced(medicamentoId) }
                }

            if (medLocal.cantidadRestante > 1) {
                val nextHora = calcularProximaHora(medLocal.hora, medLocal.frecuencia)
                val horasAIncrementar = when (medLocal.frecuencia) {
                    "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                    "Cada 12 horas" -> 12; else -> 24
                }

                val cooldownMillis = horasAIncrementar * 60 * 60 * 1000L
                val nextTiempoDesbloqueo = System.currentTimeMillis() + cooldownMillis
                val nuevoId = UUID.randomUUID().toString()

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

                val nextMedFirebase = hashMapOf(
                    "nombre" to medLocal.nombre,
                    "dosis" to medLocal.dosis,
                    "hora" to nextHora,
                    "frecuencia" to medLocal.frecuencia,
                    "estado" to "Pendiente",
                    "horaToma" to "",
                    "cantidadRestante" to (medLocal.cantidadRestante - 1),
                    "tiempoDesbloqueo" to nextTiempoDesbloqueo
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