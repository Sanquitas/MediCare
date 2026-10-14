package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val blueHeader = Color(0xFF2C699A)
    val bgGray = Color(0xFFF5F7F8)

    Column(modifier = Modifier.fillMaxSize().background(bgGray)) {
        TopAppBar(
            title = { Text("Mi Perfil", color = Color.White) },
            navigationIcon = { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 16.dp)) },
            actions = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 16.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF4CAF50))) // Fondo Verde

            // Tarjeta Perfil
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 40.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Jesús Pérez", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Paciente", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Usuario Paciente", fontSize = 12.sp)
                        Text("jesus.perez@email.com", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Información Médica", color = Color(0xFF137A7F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Tipo de Sangre", color = Color.Gray, fontSize = 12.sp); Text("O+", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        Column { Text("Edad", color = Color.Gray, fontSize = 12.sp); Text("28 años", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Contacto de Emergencia (Cuidador)", color = Color.Gray, fontSize = 12.sp)
                    Text("Mamá (María Pérez) - 555-1234", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Datos de la Cuenta / Configuración", color = Color(0xFF137A7F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
                Column {
                    ProfileOption(Icons.Default.MedicalServices, "Mis Tratamientos", true)
                    ProfileOption(Icons.Default.Notifications, "Dispositivos enlazados\nNotificaciones", false)
                    ProfileOption(Icons.Default.Map, "Datos de Ubicación y GPS", false)
                    ProfileOption(Icons.Default.CloudSync, "Sincronización con Firebase /\nDatos de respaldo", false)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, showArrow: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF137A7F))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color.Black)
        if (showArrow) Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}