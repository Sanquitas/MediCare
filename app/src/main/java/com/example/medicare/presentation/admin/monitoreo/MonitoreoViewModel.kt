package com.example.medicare.presentation.admin.monitoreo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicare.R
import com.example.medicare.domain.models.Medicamento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class PacienteMonitoreo(
    val pacienteId: String,
    val nombrePaciente: String,
    val medicamentos: List<Medicamento>
)

class MonitoreoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    var pacientesMonitoreo = mutableStateOf<List<PacienteMonitoreo>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    init {
        obtenerMonitoreoGlobal()
        iniciarEscuchaEventosPacientesEnTiempoReal()
        iniciarEscuchaPacientesAsignadosEnTiempoReal()
    }

    fun obtenerMonitoreoGlobal() {
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

                if (asignados.isEmpty()) {
                    pacientesMonitoreo.value = emptyList()
                    isLoading.value = false
                    return@launch
                }

                val usuariosSnapshot = db.collection("usuarios").get().await()
                val mapaNombres = usuariosSnapshot.documents
                    .filter { asignados.contains(it.id) }
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

                        if (userId != null && mapaNombres.containsKey(userId)) {
                            med.id = doc.id

                            if (!medicamentosAgrupados.containsKey(userId)) {
                                medicamentosAgrupados[userId] = mutableListOf()
                            }
                            medicamentosAgrupados[userId]?.add(med)
                        }
                    }
                }

                val listaFinal = mapaNombres.map { (userId, nombreFinal) ->
                    val todosMeds = medicamentosAgrupados[userId] ?: emptyList()
                    val soloFinalizados = todosMeds.filter { it.estado == "Tomado" || it.estado == "Omitido" }
                    PacienteMonitoreo(
                        pacienteId = userId,
                        nombrePaciente = nombreFinal,
                        medicamentos = soloFinalizados
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

                obtenerMonitoreoGlobal()
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

    // Escucha eventos ÚNICAMENTE de los pacientes asignados a este administrador
    private fun iniciarEscuchaEventosPacientesEnTiempoReal() {
        val adminId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val adminDoc = db.collection("usuarios").document(adminId).get().await()
                val asignados = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()

                if (asignados.isEmpty()) return@launch

                val usuariosSnapshot = db.collection("usuarios").get().await()
                val mapaNombres = usuariosSnapshot.documents
                    .filter { asignados.contains(it.id) }
                    .associate { doc -> doc.id to (doc.getString("nombre") ?: "Un paciente") }

                db.collectionGroup("medicamentos")
                    .addSnapshotListener { snapshots, e ->
                        if (e != null || snapshots == null) return@addSnapshotListener

                        for (dc in snapshots.documentChanges) {
                            if (dc.type == DocumentChange.Type.MODIFIED) {
                                val estado = dc.document.getString("estado") ?: ""
                                val nombreMed = dc.document.getString("nombre") ?: "Medicamento"
                                val userId = dc.document.reference.parent.parent?.id

                                // FILTRO RIGUROSO: Solo dispara notificación si el paciente pertenece a 'asignados'
                                if (userId != null && asignados.contains(userId)) {
                                    val nombrePaciente = mapaNombres[userId] ?: "Un paciente"

                                    if (estado == "Omitido") {
                                        dispararNotificacionAlAdmin(
                                            titulo = "¡Omisión de Medicamento!",
                                            mensaje = "$nombrePaciente ha omitido su toma de: $nombreMed"
                                        )
                                    } else if (estado == "Tomado") {
                                        dispararNotificacionAlAdmin(
                                            titulo = "¡Toma Confirmada!",
                                            mensaje = "$nombrePaciente ha enviado la confirmación de toma de: $nombreMed"
                                        )
                                    }
                                }
                            }
                        }
                    }
            } catch (_: Exception) {}
        }
    }

    // Escucha asignación de pacientes ÚNICAMENTE cuando un paciente se suma a la lista del Administrador actual
    private fun iniciarEscuchaPacientesAsignadosEnTiempoReal() {
        val adminId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                var listaAsignadosAnterior: List<String>? = null

                db.collection("usuarios").document(adminId)
                    .addSnapshotListener { adminDoc, e ->
                        if (e != null || adminDoc == null || !adminDoc.exists()) return@addSnapshotListener

                        val asignadosActuales = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()

                        if (listaAsignadosAnterior == null) {
                            listaAsignadosAnterior = asignadosActuales
                            return@addSnapshotListener
                        }

                        val nuevosIds = asignadosActuales.filter { !listaAsignadosAnterior!!.contains(it) }
                        listaAsignadosAnterior = asignadosActuales

                        if (nuevosIds.isNotEmpty()) {
                            viewModelScope.launch {
                                for (id in nuevosIds) {
                                    val userDoc = db.collection("usuarios").document(id).get().await()
                                    val nombre = userDoc.getString("nombre") ?: "Paciente"
                                    dispararNotificacionAlAdmin(
                                        titulo = "¡Nuevo Paciente Asignado!",
                                        mensaje = "Se ha asignado a tu lista de monitoreo el paciente: $nombre"
                                    )
                                }
                            }
                        }
                    }
            } catch (_: Exception) {}
        }
    }

    private fun dispararNotificacionAlAdmin(titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ADMIN_ALERTS_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Pacientes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones del sistema de monitoreo para el Administrador"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.medicare_logo)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
