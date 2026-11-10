package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.medicare.domain.models.Medicamento

@Composable
fun InicioScreen(
    nombrePaciente: String,
    medicamentos: List<Medicamento>,
    isLoading: Boolean,
    onNavigateToCapture: (String) -> Unit,
    verificarHora: (Long) -> Boolean,
    onOmitirMedicamento: (String) -> Unit
) {
    val blueHeader = Color(0xFF2C699A)
    val greenOffline = Color(0xFF4CAF50)
    val tealButton = Color(0xFF137A7F)

    val medicamentosPendientes = medicamentos.filter { it.estado == "Pendiente" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
            Row(
                modifier = Modifier.fillMaxWidth().background(blueHeader).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("MediCare", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Mi Agenda de Hoy", color = Color.White, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(greenOffline).padding(4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sincronizado con la nube", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Text("Agenda diaria", color = Color.Gray, fontSize = 14.sp)
                Text("¡Hola, $nombrePaciente!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = blueHeader)
                    }
                } else if (medicamentosPendientes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tienes medicamentos pendientes para hoy.", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(medicamentosPendientes) { med ->
                            val yaEsHora = verificarHora(med.tiempoDesbloqueo)

                            if (yaEsHora) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, blueHeader, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        Text(med.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                        Text("${med.hora} • ${med.frecuencia} • ${med.dosis}", fontSize = 14.sp, color = Color.DarkGray)
                                        Text("Faltan ${med.cantidadRestante} tomas en tu tratamiento", fontSize = 12.sp, color = blueHeader)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(onClick = { onOmitirMedicamento(med.id) }) {
                                                Text("Omitir", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { onNavigateToCapture(med.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = tealButton),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text("Confirmar Toma")
                                            }
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0E2FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(med.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                            Text("${med.hora} • ${med.frecuencia} • ${med.dosis}", fontSize = 14.sp, color = Color.DarkGray)
                                            Text("Faltan ${med.cantidadRestante} tomas en tu tratamiento", fontSize = 12.sp, color = blueHeader)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = blueHeader, modifier = Modifier.size(32.dp))
                                            Text("Próxima", fontSize = 12.sp, color = blueHeader)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
