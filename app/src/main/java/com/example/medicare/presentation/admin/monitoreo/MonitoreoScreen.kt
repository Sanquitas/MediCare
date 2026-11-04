package com.example.medicare.presentation.admin.monitoreo

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicare.domain.models.Medicamento
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MonitoreoScreen(
    viewModel: MonitoreoViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.obtenerMonitoreoGlobal()
    }
    val blueHeader = Color(0xFF2C699A)
    val tealButton = Color(0xFF137A7F)
    val backgroundGray = Color(0xFFF5F7F8)

    val pacientesMonitoreo = viewModel.pacientesMonitoreo.value
    val isLoading = viewModel.isLoading.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(blueHeader, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
            Text("Monitoreo Diario Global", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = tealButton)
                }
            } else if (pacientesMonitoreo.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No hay medicamentos registrados en el sistema.", color = Color.Gray)
                }
            } else {
                // Iteramos por cada paciente que tenga medicamentos
                pacientesMonitoreo.forEach { paciente ->
                    // Calculamos el progreso del paciente
                    val totalTomas = paciente.medicamentos.size
                    val tomasRealizadas = paciente.medicamentos.count { it.estado == "Tomado" }
                    val progreso = if (totalTomas > 0) tomasRealizadas.toFloat() / totalTomas.toFloat() else 0f

                    // Tarjeta del Paciente (Cabecera)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Paciente: ${paciente.nombrePaciente}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Estado de hoy: $tomasRealizadas de $totalTomas tomas realizadas", fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progreso },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)),
                                    color = tealButton,
                                    trackColor = Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mostramos los medicamentos de ESTE paciente
                    paciente.medicamentos.forEach { medicamento ->
                        RegistroCard(medicamento = medicamento, tealButton = tealButton)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp)) // Separación entre pacientes
                }
            }
        }
    }
}
@Composable
fun RegistroCard(medicamento: Medicamento, tealButton: Color) {
    val isTomado = medicamento.estado == "Tomado"
    val isOmitido = medicamento.estado == "Omitido"

    // Decodificamos la imagen solo si existe y recordamos el estado para no gastar memoria extra
    val imagenDecodificada = remember(medicamento.fotoEvidenciaBase64) {
        if (medicamento.fotoEvidenciaBase64.isNotEmpty()) {
            decodificarBase64(medicamento.fotoEvidenciaBase64)
        } else {
            null
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${medicamento.nombre} - ${medicamento.hora}", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                val (colorFondo, colorTexto) = when {
                    isTomado -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
                    isOmitido -> Pair(Color(0xFFF8D7DA), Color(0xFF721C24))
                    else -> Pair(Color(0xFFFFF3CD), Color(0xFF856404))
                }

                Box(modifier = Modifier.background(colorFondo, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(medicamento.estado, color = colorTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(if (isTomado) Color(0xFFE8F5E9) else Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    // Si tenemos la imagen, la dibujamos
                    if (imagenDecodificada != null) {
                        Image(
                            bitmap = imagenDecodificada,
                            contentDescription = "Evidencia fotográfica",
                            contentScale = ContentScale.Crop, // Corta la foto para que quede cuadrada perfecta
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Si no hay imagen, mostramos el ícono
                        Icon(
                            if (isTomado) Icons.Default.Image else Icons.Default.HideImage,
                            contentDescription = null,
                            tint = if (isTomado) tealButton else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = tealButton, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTomado) "Hora de toma real: ${medicamento.horaToma.ifEmpty { "Desconocida" }}"
                            else "Dosis esperada: ${medicamento.dosis}",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isTomado && imagenDecodificada != null) {
                        Button(
                            onClick = { /* Acción para abrir la foto en pantalla completa */ },
                            colors = ButtonDefaults.buttonColors(containerColor = tealButton),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ver evidencia", fontSize = 12.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                        }
                    } else if (isOmitido) {
                        Text("El paciente omitió esta dosis.", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Esperando confirmación del paciente...", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
fun decodificarBase64(base64Str: String): ImageBitmap? {
    return try {
        val cleanBase64 = base64Str.replace("\n", "").replace("\r", "")
        val imageBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.NO_WRAP)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        android.util.Log.e("MEDICARE", "Error decodificando imagen: ${e.message}")
        null
    }
}