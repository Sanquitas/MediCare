package com.example.medicare.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun loginUser(
        email: String,
        pass: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val realRole = document.getString("rol")?.trim()?.lowercase() ?: ""
                                onSuccess(realRole)
                            } else {
                                onError("No se encontraron los datos del usuario")
                            }
                        }
                        .addOnFailureListener { e -> onError("Error al leer datos: ${e.localizedMessage}") }
                }
            }
            .addOnFailureListener {
                onError("Credenciales incorrectas o usuario no registrado")
            }
    }

    fun registerUser(
        email: String, pass: String, nombre: String, rol: String,
        tipoSangre: String, edad: String, contactoEmergencia: String,
        onSuccess: () -> Unit, onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    val userData = hashMapOf(
                        "nombre" to nombre, "email" to email, "rol" to rol,
                        "tipoSangre" to tipoSangre, "edad" to edad, "contactoEmergencia" to contactoEmergencia
                    )
                    db.collection("usuarios").document(userId).set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError("Error al guardar datos: ${e.localizedMessage}") }
                }
            }
            .addOnFailureListener { e -> onError("Error al registrar: ${e.localizedMessage}") }
    }
}