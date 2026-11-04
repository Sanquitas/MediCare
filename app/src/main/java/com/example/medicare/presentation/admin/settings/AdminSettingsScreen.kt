package com.example.medicare.presentation.admin.settings

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {}
) {
    val blueHeader = Color(0xFF2C699A)
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8))) {
        TopAppBar(
            title = { Text("Ajustes y Perfil", color = Color.White, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            ) {
                Column {
                    AdminOption(Icons.Default.Person, "Gestionar mi perfil", onClick = onNavigateToProfile)
                    AdminOption(Icons.Default.Notifications, "Alertas y Notificaciones", onClick = onNavigateToNotifications)
                    AdminOption(Icons.Default.Security, "Privacidad y Seguridad", onClick = onNavigateToSecurity)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    try {
                        val intent = Intent(context, Class.forName("com.example.medicare.MainActivity"))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("MEDICARE", "Error al redirigir: ${e.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = BorderStroke(1.dp, Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF137A7F))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}