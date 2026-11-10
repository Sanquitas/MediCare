package com.example.medicare.presentation.patient.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmScheduler(private val context: Context) {

    fun programarAlarma(id: String, nombre: String, dosis: String, tiempoMilisegundos: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Alarma a la hora exacta
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("MED_ID", id)
            putExtra("MED_NOMBRE", nombre)
            putExtra("MED_DOSIS", dosis)
            putExtra("NOTIF_ID", id.hashCode())
            putExtra("TIPO_ACCION", "NOTIFICACION")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMilisegundos, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMilisegundos, pendingIntent)
        }

        // 2. Alarma de Omisión Automática pasados 60 minutos (1 Hora)
        val tiempoOmisionMs = tiempoMilisegundos + (60 * 60 * 1000L)
        val intentOmision = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("MED_ID", id)
            putExtra("MED_NOMBRE", nombre)
            putExtra("MED_DOSIS", dosis)
            putExtra("NOTIF_ID", id.hashCode() + 1)
            putExtra("TIPO_ACCION", "OMITIR_AUTOMATICO")
        }

        val pendingIntentOmision = PendingIntent.getBroadcast(
            context,
            id.hashCode() + 1,
            intentOmision,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoOmisionMs, pendingIntentOmision)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoOmisionMs, pendingIntentOmision)
        }
    }
}
