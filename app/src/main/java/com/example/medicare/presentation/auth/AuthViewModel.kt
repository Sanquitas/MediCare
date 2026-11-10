package com.example.medicare.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.medicare.data.remote.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {

    // Conectamos el repositorio de datos
    private val repository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun login(email: String, pass: String, selectedRole: String, onNavigate: (String) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage.value = "Por favor, llena todos los campos"
            return
        }

        isLoading.value = true
        errorMessage.value = null

        // Llamamos al repositorio en lugar de a Firebase directamente
        repository.loginUser(
            email = email,
            pass = pass,
            onSuccess = { realRole ->
                isLoading.value = false
                val rolElegidoLimpio = selectedRole.trim().lowercase()

                val esValidoAdmin = (realRole == "admin" || realRole == "cuidador" || realRole == "administrador") && (rolElegidoLimpio == "admin")
                val esValidoPaciente = (realRole == "paciente") && (rolElegidoLimpio == "paciente")

                if (esValidoAdmin || esValidoPaciente) {
                    // ---- CÓDIGO NUEVO: OBTENER Y GUARDAR TOKEN FCM ----
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                db.collection("usuarios").document(userId).update("fcmToken", token)
                            }
                        }
                    }
                    // ---------------------------------------------------

                    if (esValidoAdmin) onNavigate("admin")
                    else onNavigate("patient")
                } else {
                    errorMessage.value = "El rol seleccionado no corresponde a esta cuenta."
                }
            },
            onError = { errorMsg ->
                isLoading.value = false
                errorMessage.value = errorMsg
            }
        )
    }

    fun register(
        email: String, pass: String, nombre: String, rol: String,
        tipoSangre: String, edad: String, contactoEmergencia: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || pass.isBlank() || nombre.isBlank()) {
            errorMessage.value = "Llena los campos obligatorios"
            return
        }

        isLoading.value = true
        errorMessage.value = null

        repository.registerUser(
            email = email, pass = pass, nombre = nombre, rol = rol,
            tipoSangre = tipoSangre, edad = edad, contactoEmergencia = contactoEmergencia,
            onSuccess = {
                isLoading.value = false
                onSuccess()
            },
            onError = { errorMsg ->
                isLoading.value = false
                errorMessage.value = errorMsg
            }
        )
    }

    fun clearError() {
        errorMessage.value = null
    }
}