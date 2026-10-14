package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen() {
    val blueHeader = Color(0xFF2C699A)
    val bgGray = Color(0xFFF5F7F8)
    val tealButton = Color(0xFF137A7F)

    Column(modifier = Modifier.fillMaxSize().background(bgGray).verticalScroll(rememberScrollState())) {
        TopAppBar(
            title = { Text("Sincronización y Red", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 16.dp)) },
            actions = { Text("MediCare", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Card 1: Wifi
            SyncCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF28A745), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Estado Actual:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Conectado (Wi-Fi)", color = Color(0xFF28A745), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Modo Online: Datos listos para respaldar en la nube", fontSize = 12.sp, color = Color.DarkGray)
            }

            // Card 2: Firebase
            SyncCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Base de Datos en la Nube (Firebase):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sincronizado", color = Color(0xFF28A745), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF28A745), modifier = Modifier.size(18.dp))
                        }
                        Text("Última sincronización: Hace 5 minutos", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }

            // Card 3: Room DB
            SyncCard {
                Text("Datos Pendientes (Almacenados en Room)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tomas en espera de subir: 0", fontSize = 14.sp)
                Text("Imágenes por respaldar: 0", fontSize = 14.sp)
                Text("3 imágenes subidas con éxito", fontSize = 14.sp, color = Color.DarkGray)
            }

            // Card 4: Progreso
            SyncCard {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CircularProgressIndicator(progress = 1f, color = tealButton, modifier = Modifier.size(40.dp), strokeWidth = 6.dp)
                    Text("Sincronización al 100%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Sync, contentDescription = null, tint = tealButton)
                }
            }

            // Card 5: Configuración
            SyncCard {
                Text("Configuración de Sincronización", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sincronizar solo con Wi-Fi", fontSize = 14.sp)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = tealButton, checkedTrackColor = tealButton.copy(alpha = 0.5f)))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sincronizar en segundo plano", fontSize = 14.sp)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = tealButton, checkedTrackColor = tealButton.copy(alpha = 0.5f)))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = tealButton),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Sincronizar Ahora", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SyncCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}