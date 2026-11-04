package com.example.medicare.presentation.capture

import android.app.Application
import android.content.Context // NUEVO
import android.graphics.Bitmap
import android.net.ConnectivityManager // NUEVO
import android.net.NetworkCapabilities // NUEVO
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.data.local.MedicareDatabase
import com.example.medicare.data.local.entities.MedicamentoEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // NUEVO
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class CaptureViewModel(application: Application) : AndroidViewModel(application) {
    private val medicamentoDao = MedicareDatabase.getDatabase(application).medicamentoDao()

    // Agregamos la conexión a Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    private fun guardarImagenLocal(bitmap: Bitmap): String {
        val context = getApplication<Application>()
        val file = File(context.filesDir, "evidencia_${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()
        return file.absolutePath
    }

    fun confirmarToma(
        medicamentoId: String,
        fotoBitmap: Bitmap?,
        ubicacion: String,
        onSuccess: () -> Unit
    ) {
        if (fotoBitmap == null || ubicacion.isBlank() || ubicacion.contains("Buscando")) {
            errorMessage.value = "Evidencia incompleta."
            return
        }

        isLoading.value = true
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // --- 1. VERIFICAR REGLA DE SINCRONIZACIÓN Y RED ---
                val userDoc = db.collection("usuarios").document(userId).get().await()
                val soloWifi = userDoc.getBoolean("soloWifi") ?: false

                val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

                val puedeSubirAFirebase = !soloWifi || isWifi

                // --- 2. PROCESAR IMAGEN A BASE64 (Solo si se va a subir) ---
                var fotoBase64 = ""
                if (puedeSubirAFirebase) {
                    try {
                        val maxResolution = 600f
                        val ratio = Math.min(maxResolution / fotoBitmap.width, maxResolution / fotoBitmap.height)
                        val width = Math.round(ratio * fotoBitmap.width)
                        val height = Math.round(ratio * fotoBitmap.height)

                        val resizedBitmap = Bitmap.createScaledBitmap(fotoBitmap, width, height, true)
                        val baos = ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                        val imageBytes = baos.toByteArray()
                        fotoBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    } catch (e: Exception) {
                        android.util.Log.e("MEDICARE", "Error convirtiendo imagen a Base64: ${e.message}")
                    }
                }

                // --- 3. GUARDADO LOCAL (Siempre se hace) ---
                val rutaFoto = guardarImagenLocal(fotoBitmap)
                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
                val horaRealToma = sdf.format(java.util.Calendar.getInstance().time)

                val med = medicamentoDao.getMedicamentoById(medicamentoId)

                if (med != null) {
                    // Actualizamos Room (Base local)
                    medicamentoDao.registrarTomaConFoto(medicamentoId, horaRealToma, rutaFoto)

                    // --- 4. SUBIR A FIREBASE (Si cumple la regla de red) ---
                    if (puedeSubirAFirebase) {
                        db.collection("usuarios").document(userId)
                            .collection("medicamentos").document(medicamentoId)
                            .update(
                                mapOf(
                                    "estado" to "Tomado",
                                    "horaToma" to horaRealToma,
                                    "fotoEvidenciaBase64" to fotoBase64
                                )
                            )
                            .addOnSuccessListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    medicamentoDao.markAsSynced(medicamentoId)
                                }
                            }
                    }

                    if (med.cantidadRestante > 1) {
                        val sdfCalc = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ROOT)
                        val dateObj = sdfCalc.parse(med.hora.trim().uppercase(java.util.Locale.ROOT))
                        val cal = java.util.Calendar.getInstance()
                        if (dateObj != null) cal.time = dateObj

                        val horas = when (med.frecuencia) {
                            "Cada 4 horas" -> 4; "Cada 6 horas" -> 6; "Cada 8 horas" -> 8
                            "Cada 12 horas" -> 12; else -> 24
                        }
                        cal.add(java.util.Calendar.HOUR_OF_DAY, horas)

                        // Corrección matemática limpia
                        val cooldownMillis = horas * 60 * 60 * 1000L
                        val nextTiempoDesbloqueo = System.currentTimeMillis() + cooldownMillis
                        val nuevoId = UUID.randomUUID().toString()

                        val nextMed = MedicamentoEntity(
                            id = nuevoId,
                            nombre = med.nombre,
                            dosis = med.dosis,
                            hora = sdfCalc.format(cal.time),
                            frecuencia = med.frecuencia,
                            estado = "Pendiente",
                            horaToma = "",
                            cantidadRestante = med.cantidadRestante - 1,
                            tiempoDesbloqueo = nextTiempoDesbloqueo,
                            fotoLocalUri = null,
                            isSynced = false
                        )
                        medicamentoDao.insertMedicamento(nextMed)

                        if (puedeSubirAFirebase) {
                            val nextMedFirebase = hashMapOf(
                                "nombre" to med.nombre,
                                "dosis" to med.dosis,
                                "hora" to sdfCalc.format(cal.time),
                                "frecuencia" to med.frecuencia,
                                "estado" to "Pendiente",
                                "horaToma" to "",
                                "cantidadRestante" to (med.cantidadRestante - 1),
                                "tiempoDesbloqueo" to nextTiempoDesbloqueo
                            )
                            db.collection("usuarios").document(userId).collection("medicamentos")
                                .document(nuevoId).set(nextMedFirebase)
                        }
                    }
                }

                launch(Dispatchers.Main) {
                    isLoading.value = false
                    if (!puedeSubirAFirebase) {
                        errorMessage.value = "Guardado localmente. Esperando Wi-Fi para subir evidencia."
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    isLoading.value = false
                    errorMessage.value = "Error al guardar: ${e.message}"
                }
            }
        }
    }
}