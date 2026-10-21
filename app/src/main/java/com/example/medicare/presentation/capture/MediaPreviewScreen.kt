package com.example.medicare.presentation.capture

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    medicamentoId: String,
    ubicacion: String,
    foto: Bitmap?,
    permisosListos: Boolean,
    onRequestPermissions: () -> Unit,
    onTakePhoto: () -> Unit,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val blueHeader = Color(0xFF1E6C93)
    val tealButton = Color(0xFF137A7F)

    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value

    // CANDADO ESTRICTO: Solo es "true" si hay foto Y el texto del GPS es válido
    val sePuedeGuardar = permisosListos &&
            ubicacion != "Buscando GPS..." &&
            ubicacion != "GPS inactivo, enciende tu ubicación" &&
            ubicacion != "Error al obtener GPS" &&
            foto != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Toma", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)).padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Cámara", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray).clickable { onTakePhoto() },
                contentAlignment = Alignment.Center
            ) {
                if (foto != null) {
                    Image(bitmap = foto.asImageBitmap(), contentDescription = "Foto capturada", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text("Presiona para capturar", color = Color.White)
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (24).dp).size(48.dp).background(tealButton, CircleShape).border(4.dp, Color(0xFFF0F0F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(60.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (permisosListos) ubicacion else "Se requiere acceso a ubicación", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (sePuedeGuardar) Color(0xFF28A745) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sePuedeGuardar) "Ubicación confirmada" else "Esperando señal...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sePuedeGuardar) Color(0xFF28A745) else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = permisosListos,
                    onCheckedChange = { if (it) onRequestPermissions() },
                    colors = SwitchDefaults.colors(checkedThumbColor = tealButton, checkedTrackColor = tealButton.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permiso de GPS y Cámara concedidos", fontSize = 14.sp)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(color = tealButton)
            } else {
                Button(
                    onClick = {
                        viewModel.confirmarToma(medicamentoId, foto, ubicacion, onSaveSuccess)
                    },
                    enabled = sePuedeGuardar, // Bloqueo visual estricto
                    colors = ButtonDefaults.buttonColors(containerColor = tealButton, disabledContainerColor = Color.LightGray),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = if (sePuedeGuardar) "Sincronizar Toma" else "Completa la foto y la ubicación",
                        color = if (sePuedeGuardar) Color.White else Color.DarkGray,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}