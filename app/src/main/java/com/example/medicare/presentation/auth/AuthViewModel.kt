package com.example.medicare.presentation.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
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

        auth.signInWithEmailAndPassword(email.trim(), pass.trim())
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener { doc ->
                        isLoading.value = false
                        val rolEnBd = doc.getString("rol") ?: "paciente"

                        if (rolEnBd == selectedRole) {
                            onNavigate(rolEnBd)
                        } else {
                            errorMessage.value = "El usuario no cuenta con el rol de ${if (selectedRole == "admin") "Administrador" else "Paciente"}"
                            auth.signOut()
                        }
                    }
                    .addOnFailureListener {
                        isLoading.value = false
                        errorMessage.value = "Error al verificar el rol del usuario"
                    }
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                errorMessage.value = e.localizedMessage ?: "Error al iniciar sesión"
            }
    }

    fun registrar(nombre: String, email: String, pass: String, selectedRole: String, onSuccess: () -> Unit) {
        if (nombre.isBlank() || email.isBlank() || pass.isBlank()) {
            errorMessage.value = "Por favor, completa todos los campos"
            return
        }

        isLoading.value = true
        errorMessage.value = null

        auth.createUserWithEmailAndPassword(email.trim(), pass.trim())
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                val usuarioData = hashMapOf(
                    "nombre" to nombre,
                    "email" to email.trim(),
                    "rol" to selectedRole
                )

                db.collection("usuarios").document(userId).set(usuarioData)
                    .addOnSuccessListener {
                        isLoading.value = false
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        isLoading.value = false
                        errorMessage.value = "Se creó el usuario pero hubo un error al guardar los datos: ${e.localizedMessage}"
                    }
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                errorMessage.value = e.localizedMessage ?: "Error al registrar el usuario"
            }
    }

    fun clearError() {
        errorMessage.value = null
    }
}