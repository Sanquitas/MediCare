package com.example.medicare.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun MonitoreoScreen() {
    val blueHeader = Color(0xFF2C699A)
    val tealButton = Color(0xFF137A7F)
    val backgroundGray = Color(0xFFF5F7F8)

    Column(modifier = Modifier.fillMaxSize().background(backgroundGray).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().background(blueHeader, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = blueHeader)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("MediCare Admin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                Text("Admin", color = Color.White, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Paciente: Jesús Pérez", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Estado de hoy: 2 de 3 tomas realizadas", fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = 0.66f, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)), color = tealButton, trackColor = Color(0xFFE0E0E0))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = tealButton), shape = RoundedCornerShape(50.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Tratamiento", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Monitoreo Diario", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            RegistroCard("Registro 1 (Paracetamol - 08:00 AM)", tealButton)
            Spacer(modifier = Modifier.height(12.dp))
            RegistroCard("Registro 2 (Ibuprofeno - 02:00 PM)", tealButton)
            Spacer(modifier = Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registro 3 (Complejo B - 09:00 PM)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.background(Color(0xFFFFF3CD), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Pendiente", color = Color(0xFF856404), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Próxima toma en 1 hora", fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RegistroCard(titulo: String, tealButton: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = tealButton, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubicación confirmada\n(Toluca, Mex.)", fontSize = 12.sp, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = tealButton), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) {
                        Text("Ver evidencia", fontSize = 12.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}