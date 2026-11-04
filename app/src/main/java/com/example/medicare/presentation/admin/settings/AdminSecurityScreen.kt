package com.example.medicare.presentation.admin.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSecurityScreen(
    onBack: () -> Unit = {},
    viewModel: AdminSettingsViewModel = viewModel()
) {
    val blueHeader = Color(0xFF2C699A)
    val tealButton = Color(0xFF137A7F)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidad y Seguridad", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8)).padding(padding).padding(16.dp)) {
            Text("Reglas de Privacidad", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SecuritySwitchRow("Modo discreto", "Ocultar apellidos de pacientes", viewModel.ocultarDatosSensibles.value) {
                        viewModel.actualizarPreferencia("modoDiscreto", it)
                    }
                    Divider(color = Color(0xFFE0E0E0))
                    SecuritySwitchRow("Sincronización segura", "Subir reportes solo con Wi-Fi", viewModel.accesoSoloWifi.value) {
                        viewModel.actualizarPreferencia("soloWifi", it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Credenciales", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // LÓGICA REAL DE FIREBASE PARA CAMBIO DE CONTRASEÑA
                    val auth = FirebaseAuth.getInstance()
                    val email = auth.currentUser?.email
                    if (email != null) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Correo enviado a $email para restablecer contraseña", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealButton),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar mi contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecuritySwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF137A7F))
        )
    }
}