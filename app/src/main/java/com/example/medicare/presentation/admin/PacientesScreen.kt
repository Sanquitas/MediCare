package com.example.medicare.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
fun PacientesScreen() {
    val blueHeader = Color(0xFF2C699A)
    val tealButton = Color(0xFF137A7F)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8))) {
        TopAppBar(
            title = { Text("Pacientes Asignados", color = Color.White, fontWeight = FontWeight.Bold) },
            actions = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 16.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = "", onValueChange = {}, placeholder = { Text("Buscar paciente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))
            PacienteCard("Jesús Pérez", "2 Tratamientos Activos", "Buena adherencia", Color(0xFF28A745))
            PacienteCard("María Gómez", "1 Tratamiento Activo", "Requiere atención", Color(0xFFFFC107))
        }
    }
}

@Composable
fun PacienteCard(nombre: String, tratamientos: String, estado: String, colorEstado: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(tratamientos, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colorEstado))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(estado, fontSize = 12.sp, color = colorEstado, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}