package com.example.medicare.presentation.patient.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.medicare.R
import com.example.medicare.data.local.MedicareDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tipoAccion = intent.getStringExtra("TIPO_ACCION") ?: "NOTIFICACION"
        val medicamentoId = intent.getStringExtra("MED_ID") ?: ""
        val medicamentoNombre = intent.getStringExtra("MED_NOMBRE") ?: "tu medicamento"
        val medicamentoDosis = intent.getStringExtra("MED_DOSIS") ?: ""
        val notificacionId = intent.getIntExtra("NOTIF_ID", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MEDICARE_CHANNEL",
                "Recordatorios de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para avisar la hora de las medicinas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (tipoAccion == "OMITIR_AUTOMATICO" && medicamentoId.isNotEmpty()) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val auth = FirebaseAuth.getInstance()
                    val database = MedicareDatabase.getDatabase(context)
                    val dao = database.medicamentoDao()

                    val medLocal = dao.getMedicamentoById(medicamentoId)
                    if (medLocal != null && medLocal.estado == "Pendiente") {
                        val sdfHoraReal = SimpleDateFormat("hh:mm a", Locale.ROOT)
                        val horaTomaReal = sdfHoraReal.format(Calendar.getInstance().time)

                        dao.updateEstadoYHora(medicamentoId, "Omitido", horaTomaReal, System.currentTimeMillis())

                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            db.collection("usuarios").document(userId)
                                .collection("medicamentos").document(medicamentoId)
                                .update(
                                    mapOf(
                                        "estado" to "Omitido",
                                        "horaToma" to horaTomaReal,
                                        "fechaRegistro" to System.currentTimeMillis()
                                    )
                                )
                        }

                        val builder = NotificationCompat.Builder(context, "MEDICARE_CHANNEL")
                            .setSmallIcon(R.drawable.medicare_logo)
                            .setContentTitle("Dosis Omitida Automáticamente")
                            .setContentText("Pasó 1 hora desde la hora fijada para $medicamentoNombre. Se ha marcado como omitida.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)

                        notificationManager.notify(notificacionId, builder.build())
                    }
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val builder = NotificationCompat.Builder(context, "MEDICARE_CHANNEL")
            .setSmallIcon(R.drawable.medicare_logo)
            .setContentTitle("¡Es hora de tu medicina!")
            .setContentText("Toma $medicamentoDosis de $medicamentoNombre ahora.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificacionId, builder.build())
    }
}
