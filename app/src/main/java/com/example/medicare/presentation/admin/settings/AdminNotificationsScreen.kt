package com.example.medicare.presentation.admin.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(
    onBack: () -> Unit = {},
    viewModel: AdminSettingsViewModel = viewModel()
) {
    val blueHeader = Color(0xFF2C699A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas y Notificaciones", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8)).padding(padding).padding(16.dp)) {
            Text("Configuración de Alertas", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    NotificationSwitchRow("Tomas omitidas", "Avisar cuando un paciente no tome su medicamento", viewModel.omitidas.value) {
                        viewModel.actualizarPreferencia("notifOmitidas", it)
                    }
                    Divider(color = Color(0xFFE0E0E0))
                    NotificationSwitchRow("Resumen diario", "Recibir un reporte al final del día", viewModel.resumenDiario.value) {
                        viewModel.actualizarPreferencia("notifResumen", it)
                    }
                    Divider(color = Color(0xFFE0E0E0))
                    NotificationSwitchRow("Nuevos pacientes", "Avisar al asignar un paciente nuevo", viewModel.nuevosPacientes.value) {
                        viewModel.actualizarPreferencia("notifNuevos", it)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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