package com.example.medicare.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onTratamientosClick: () -> Unit,
    onAjustesClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val user = viewModel.patientData.value
    val isLoading = viewModel.isLoading.value

    val blueHeader = Color(0xFF2E6B9E)
    val greenBanner = Color(0xFF5DB359)
    val backgroundLight = Color(0xFFF5F6F8)
    val tealText = Color(0xFF1E7C81)

    Column(modifier = Modifier.fillMaxSize().background(backgroundLight)) {
        // 1. Barra Superior (Azul)
        Row(
            modifier = Modifier.fillMaxWidth().background(blueHeader).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.clickable { onNavigateBack() })
                Spacer(modifier = Modifier.width(16.dp))
                Text("Mi Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White, modifier = Modifier.clickable { viewModel.cerrarSesion(onLogout) })
        }

        // 2. Encabezado Verde y Tarjeta de Usuario Superpuesta
        Box(modifier = Modifier.fillMaxWidth()) {
            // Fondo verde
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(greenBanner))

            // Tarjeta Blanca Principal
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp).border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(16.dp))
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = tealText)
                    }
                } else {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFD9D9D9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(user.rol, fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Usuario ${user.rol}", fontSize = 12.sp, color = Color.DarkGray)
                            Text(user.email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // 3. Sección de Información Médica
        Text("Información Médica", color = tealText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBEBEB)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tipo de Sangre", color = Color.Gray, fontSize = 12.sp)
                        Text(user.tipoSangre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Edad", color = Color.Gray, fontSize = 12.sp)
                        Text("${user.edad} años", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Contacto de Emergencia", color = Color.Gray, fontSize = 12.sp)
                Text(user.contactoEmergencia, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Sección de Configuración
        Text("Datos de la Cuenta / Configuración", color = Color(0xFF1E7C81), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBEBEB)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column {
                ConfigItem(icon = Icons.Default.MedicalServices, text = "Mis Tratamientos", onClick = onTratamientosClick)
                Divider(color = Color(0xFFF0F0F0))

                ConfigItem(icon = Icons.Default.Security, text = "Privacidad, Alertas y GPS", onClick = onAjustesClick)

                Divider(color = Color(0xFFF0F0F0))
                ConfigItem(icon = Icons.Default.CloudSync, text = "Sincronización con Firebase", hasArrow = false, onClick = onSyncClick)
            }
        }
    }
}

@Composable
fun ConfigItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, hasArrow: Boolean = true, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF1E7C81), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 14.sp, color = Color.Black, modifier = Modifier.weight(1f))
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}