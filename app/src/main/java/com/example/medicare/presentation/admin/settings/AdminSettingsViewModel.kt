package com.example.medicare.presentation.admin.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // IMPORTACIÓN CLAVE

class AdminSettingsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    var omitidas = mutableStateOf(true)
    var resumenDiario = mutableStateOf(false)
    var nuevosPacientes = mutableStateOf(true)

    var ocultarDatosSensibles = mutableStateOf(false)
    var accesoSoloWifi = mutableStateOf(true)

    init {
        cargarConfiguracion()
    }

    private fun cargarConfiguracion() {
        if (userId == null) return

        db.collection("usuarios").document(userId).addSnapshotListener { doc, error ->
            if (error != null) {
                android.util.Log.e("MEDICARE", "Error escuchando ajustes: ${error.message}")
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                omitidas.value = doc.getBoolean("notifOmitidas") ?: true
                resumenDiario.value = doc.getBoolean("notifResumen") ?: false
                nuevosPacientes.value = doc.getBoolean("notifNuevos") ?: true
                ocultarDatosSensibles.value = doc.getBoolean("modoDiscreto") ?: false
                accesoSoloWifi.value = doc.getBoolean("soloWifi") ?: true
            }
        }
    }

    fun actualizarPreferencia(campo: String, valor: Boolean) {
        if (userId == null) return

        // 1. Actualización Optimista (Movemos la UI al instante para que sea fluida)
        when (campo) {
            "notifOmitidas" -> omitidas.value = valor
            "notifResumen" -> resumenDiario.value = valor
            "notifNuevos" -> nuevosPacientes.value = valor
            "modoDiscreto" -> ocultarDatosSensibles.value = valor
            "soloWifi" -> accesoSoloWifi.value = valor
        }

        val datoAModificar = mapOf(campo to valor)

        db.collection("usuarios").document(userId)
            .set(datoAModificar, SetOptions.merge())
            .addOnFailureListener {
                android.util.Log.e("MEDICARE", "Error al guardar ajuste en Firebase: $campo")

                when (campo) {
                    "notifOmitidas" -> omitidas.value = !valor
                    "notifResumen" -> resumenDiario.value = !valor
                    "notifNuevos" -> nuevosPacientes.value = !valor
                    "modoDiscreto" -> ocultarDatosSensibles.value = !valor
                    "soloWifi" -> accesoSoloWifi.value = !valor
                }
            }
    }
}