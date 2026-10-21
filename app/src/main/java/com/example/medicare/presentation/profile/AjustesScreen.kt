package com.example.medicare.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onNavigateBack: () -> Unit
) {
    val backgroundLight = Color(0xFFF5F6F8)
    val tealText = Color(0xFF1E7C81)
    val cardBackground = Color.White

    var alarmasActivas by remember { mutableStateOf(true) }
    var gpsConcedido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(backgroundLight)) {

        // PANEL SUPERIOR (Hero Header) CON GRADIENTE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E6B9E), Color(0xFF1E7C81))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // TopBar transparente
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.clickable { onNavigateBack() })
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Privacidad y Alertas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Ícono central decorativo
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 1: NOTIFICACIONES (Tarjeta Elevada)
        Text("Gestión de Alertas", color = tealText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (alarmasActivas) Color(0xFFE8F4F5) else Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (alarmasActivas) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff, contentDescription = null, tint = if (alarmasActivas) tealText else Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Interruptor Maestro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                        Text(if (alarmasActivas) "Sistema de recordatorios activo" else "Todas las alarmas silenciadas", fontSize = 13.sp, color = Color.Gray)
                    }
                }
                Switch(
                    checked = alarmasActivas,
                    onCheckedChange = { alarmasActivas = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = tealText)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECCIÓN 2: GPS CON DISEÑO DE MAPA
        Text("Rastreo y Ubicación", color = tealText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Cabecera del GPS
                Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(if (gpsConcedido) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (gpsConcedido) Icons.Default.GpsFixed else Icons.Default.GpsOff, contentDescription = null, tint = if (gpsConcedido) Color(0xFF388E3C) else Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (gpsConcedido) "Conexión GPS Establecida" else "Acceso GPS Bloqueado",
                            color = if (gpsConcedido) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Cuerpo y Botón
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("MediCare requiere acceso a tu ubicación únicamente al momento de registrar la toma de un medicamento para el historial médico.", fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { gpsConcedido = !gpsConcedido },
                        colors = ButtonDefaults.buttonColors(containerColor = if (gpsConcedido) Color(0xFFF0F0F0) else tealText),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (gpsConcedido) 0.dp else 4.dp)
                    ) {
                        Icon(if (gpsConcedido) Icons.Default.Close else Icons.Default.Check, contentDescription = null, tint = if (gpsConcedido) Color.DarkGray else Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gpsConcedido) "Revocar Acceso" else "Otorgar Permiso de GPS",
                            color = if (gpsConcedido) Color.DarkGray else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}