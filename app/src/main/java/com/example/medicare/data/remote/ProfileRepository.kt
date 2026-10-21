package com.example.medicare.data.remote

import com.example.medicare.domain.models.UserPatient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun obtenerPerfilUsuario(onSuccess: (UserPatient) -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("Usuario no autenticado")
            return
        }

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = UserPatient(
                        nombre = document.getString("nombre") ?: "Sin nombre",
                        email = document.getString("email") ?: auth.currentUser?.email ?: "",
                        rol = document.getString("rol")?.replaceFirstChar { it.uppercase() } ?: "Paciente",
                        tipoSangre = document.getString("tipoSangre") ?: "N/A",
                        edad = document.getString("edad") ?: "N/A",
                        contactoEmergencia = document.getString("contactoEmergencia") ?: "N/A"
                    )
                    onSuccess(user)
                } else {
                    onError("El perfil no existe en la base de datos")
                }
            }
            .addOnFailureListener { e ->
                onError("Error al obtener perfil: ${e.localizedMessage}")
            }
    }

    // De paso agregamos la función para cerrar sesión desde el perfil
    fun cerrarSesion() {
        auth.signOut()
    }
}