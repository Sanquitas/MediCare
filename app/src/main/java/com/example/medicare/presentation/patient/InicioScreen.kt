package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicare.domain.models.Medicamento

@Composable
fun InicioScreen(
    nombrePaciente: String,
    medicamentos: List<Medicamento>,
    isLoading: Boolean,
    onNavigateToCapture: (String) -> Unit,
    onAgregarMedicamento: (String, String, String, String, Int) -> Unit,
    verificarHora: (Long) -> Boolean, // AHORA RECIBE UN LONG
    onOmitirMedicamento: (String) -> Unit
) {
    val blueHeader = Color(0xFF2C699A)
    val greenOffline = Color(0xFF4CAF50)
    val tealButton = Color(0xFF137A7F)

    var showAddDialog by remember { mutableStateOf(false) }
    val medicamentosPendientes = medicamentos.filter { it.estado == "Pendiente" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
            Row(modifier = Modifier.fillMaxWidth().background(blueHeader).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
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

            Row(modifier = Modifier.fillMaxWidth().background(greenOffline).padding(4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
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
                            // ESTE ES EL BLOQUEADOR: Evaluamos si ya pasó el tiempo de enfriamiento
                            val yaEsHora = verificarHora(med.tiempoDesbloqueo) // Verificamos la variable del candado

                            if (yaEsHora) {
                                // TARJETA BLANCA: Ya es hora, botones liberados
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, blueHeader, RoundedCornerShape(12.dp))) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        Text(med.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                        Text("${med.hora} • ${med.frecuencia} • ${med.dosis}", fontSize = 14.sp, color = Color.DarkGray)
                                        Text("Faltan ${med.cantidadRestante} tomas en tu tratamiento", fontSize = 12.sp, color = blueHeader)

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                            TextButton(onClick = { onOmitirMedicamento(med.id) }) {
                                                Text("Omitir", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(onClick = { onNavigateToCapture(med.id) }, colors = ButtonDefaults.buttonColors(containerColor = tealButton), shape = RoundedCornerShape(20.dp)) {
                                                Text("Confirmar Toma")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // TARJETA AZUL (BLOQUEADOR): Aún no pasan las X horas, no hay botones
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD0E2FF)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = tealButton,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar medicamento")
        }
    }

    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, dosis, hora, frecuencia, cantidad ->
                onAgregarMedicamento(nombre, dosis, hora, frecuencia, cantidad)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, Int) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("") }
    var cantidadTotal by remember { mutableStateOf("") }

    var expandedFrecuencia by remember { mutableStateOf(false) }
    val opcionesFrecuencia = listOf("Cada 4 horas", "Cada 6 horas", "Cada 8 horas", "Cada 12 horas", "Cada 24 horas (Una vez al día)")

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Medicamento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it; showError = false }, label = { Text("Nombre (ej. Paracetamol)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dosis, onValueChange = { dosis = it; showError = false }, label = { Text("Dosis (ej. 1 tableta)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = hora, onValueChange = { hora = it; showError = false }, label = { Text("Hora planeada (ej. 08:00 AM)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedFrecuencia,
                    onExpandedChange = { expandedFrecuencia = !expandedFrecuencia; showError = false }
                ) {
                    OutlinedTextField(
                        value = frecuencia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia / Horario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrecuencia,
                        onDismissRequest = { expandedFrecuencia = false }
                    ) {
                        opcionesFrecuencia.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    frecuencia = seleccion
                                    expandedFrecuencia = false
                                    showError = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cantidadTotal,
                    onValueChange = { cantidadTotal = it; showError = false },
                    label = { Text("Total de tomas (ej. 15)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = cantidadTotal.toIntOrNull() ?: 0
                    val horaRegex = Regex("^(0?[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM|am|pm)$")

                    when {
                        nombre.trim().length < 3 -> { errorMessage = "El nombre debe tener al menos 3 letras."; showError = true }
                        dosis.trim().isEmpty() -> { errorMessage = "La dosis no puede estar vacía."; showError = true }
                        !hora.trim().matches(horaRegex) -> { errorMessage = "Formato de hora inválido. Usa ej: 08:00 AM"; showError = true }
                        frecuencia.isEmpty() -> { errorMessage = "Debes seleccionar una frecuencia."; showError = true }
                        total <= 0 -> { errorMessage = "El total de tomas debe ser mayor a 0."; showError = true }
                        else -> {
                            showError = false
                            onConfirm(nombre.trim(), dosis.trim(), hora.trim().uppercase(), frecuencia, total)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A7F))
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}