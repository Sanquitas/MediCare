package com.example.medicare.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
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
fun AdminHistorialScreen() {
    val blueHeader = Color(0xFF2C699A)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8))) {
        TopAppBar(
            title = { Text("Historial de Tomas", color = Color.White, fontWeight = FontWeight.Bold) },
            actions = { Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 16.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Últimos registros de todos los pacientes", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            AdminHistoryCard("Jesús Pérez", "Paracetamol", "08:03 AM - Hoy", true)
            AdminHistoryCard("María Gómez", "Losartán", "07:30 AM - Hoy", true)
        }
    }
}

@Composable
fun AdminHistoryCard(paciente: String, medicamento: String, hora: String, verificado: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(paciente, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C699A))
                Text("$medicamento • $hora", fontSize = 14.sp, color = Color.DarkGray)
            }
            if (verificado) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF28A745))
            }
        }
    }
}