package com.example.medicare.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicare.presentation.sync.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    syncViewModel: SyncViewModel = viewModel()
) {
    val pendientes = syncViewModel.pendientesDeSincronizar.value
    val isSyncing = syncViewModel.isSyncing.value
    val progress = syncViewModel.syncProgress.value

    val bgColor = Color(0xFFEBEBEB) // Fondo gris claro de la imagen
    val primaryColor = Color(0xFF3373B0) // Azul del TopBar
    val tealColor = Color(0xFF227E74) // Verde azulado del botón y progreso
    val successColor = Color(0xFF388E3C) // Verde de éxito

    LaunchedEffect(Unit) {
        syncViewModel.verificarPendientes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sincronización y Red", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("MediCare", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Acción de regreso si es necesaria */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 1: Estado de Red
            SyncCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi", tint = successColor, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Estado Actual:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(syncViewModel.networkStatus.value, fontSize = 18.sp, color = successColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(syncViewModel.networkSubtext.value, fontSize = 12.sp, color = Color.DarkGray)
            }

            // Tarjeta 2: Firebase
            SyncCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sustituto de logo de Firebase
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "Firebase", tint = Color(0xFFFFA000), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Base de Datos en la Nube (Firebase):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sincronizado", fontSize = 18.sp, color = successColor, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = successColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Última sincronización: ${syncViewModel.lastSyncTime.value}", fontSize = 12.sp, color = Color.DarkGray)
            }

            // Tarjeta 3: Room
            SyncCard {
                Text("Datos Pendientes (Almacenados en Room)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tomas en espera de subir: ${pendientes.size}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Imágenes por respaldar: ${syncViewModel.pendingImagesCount.value}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${syncViewModel.uploadedImagesCount.value} imágenes subidas con éxito", fontSize = 14.sp)
            }

            // Tarjeta 4: Progreso
            SyncCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(40.dp),
                            color = tealColor,
                            trackColor = Color.LightGray,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Sincronización al ${(progress * 100).toInt()}%", fontSize = 16.sp)
                    }
                    IconButton(onClick = { syncViewModel.verificarPendientes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = tealColor)
                    }
                }
            }

            // Tarjeta 5: Configuración
            SyncCard {
                Text("Configuración de Sincronización", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sincronizar solo con Wi-Fi", fontSize = 14.sp)
                    Switch(
                        checked = syncViewModel.isWifiOnlyEnabled.value,
                        onCheckedChange = { syncViewModel.toggleWifiOnly(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = tealColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de sincronización
            Button(
                onClick = { syncViewModel.sincronizarDatos() },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isSyncing) "Sincronizando..." else "Sincronizar Ahora",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SyncCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}