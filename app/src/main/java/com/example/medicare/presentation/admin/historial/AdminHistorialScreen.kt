package com.example.medicare.presentation.admin.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistorialScreen(
    viewModel: AdminHistorialViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.obtenerHistorial()
    }
    val blueHeader = Color(0xFF2C699A)
    val historialList = viewModel.historial.value
    val isLoading = viewModel.isLoading.value

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8))) {
        TopAppBar(
            title = { Text("Historial de Tomas", color = Color.White, fontWeight = FontWeight.Bold) },
            actions = { Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 16.dp)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Últimos registros de todos los pacientes", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueHeader)
                }
            } else if (historialList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros en el historial aún.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(historialList) { item ->
                        AdminHistoryCard(
                            paciente = item.nombrePaciente,
                            medicamento = item.nombreMedicamento,
                            hora = item.horaTomaReal,
                            estado = item.estado
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHistoryCard(paciente: String, medicamento: String, hora: String, estado: String) {
    val isTomado = estado == "Tomado"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(paciente, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C699A))
                Text("$medicamento - $hora", fontSize = 14.sp, color = Color.DarkGray)
            }
            if (isTomado) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Tomado", tint = Color(0xFF28A745))
            } else {
                Icon(Icons.Default.Cancel, contentDescription = "Omitido", tint = Color(0xFFD32F2F))
            }
        }
    }
}