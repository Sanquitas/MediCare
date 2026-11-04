package com.example.medicare.presentation.admin.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicare.domain.models.UserPatient
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacientesScreen(
    viewModel: PacientesViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.obtenerPacientes()
    }

    val pacientes = viewModel.pacientes.value
    val isLoading = viewModel.isLoading.value
    val searchQuery = viewModel.searchQuery.value

    val blueHeader = Color(0xFF2C699A)
    val tealButton = Color(0xFF137A7F)

    var pacienteSeleccionado by remember { mutableStateOf<UserPatient?>(null) }
    var showAsignarDialog by remember { mutableStateOf(false) } // Controla el menú del botón +

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8))) {
        TopAppBar(
            title = { Text("Pacientes Asignados", color = Color.White, fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { showAsignarDialog = true }, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Asignar Pacientes", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar paciente...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueHeader)
                }
            } else if (pacientes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Toca el botón '+' arriba a la derecha para asignar pacientes a tu lista.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(pacientes) { paciente ->
                        val nombreMostrar = paciente.nombre.ifEmpty { "Paciente sin nombre" }
                        val datosExtra = "Edad: ${paciente.edad} | Sangre: ${paciente.tipoSangre}"

                        PacienteCard(
                            nombre = nombreMostrar,
                            tratamientos = datosExtra,
                            estado = "Registrado",
                            colorEstado = Color(0xFF28A745),
                            onClick = { pacienteSeleccionado = paciente }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO PARA ASIGNAR PACIENTES (Boton +)
    if (showAsignarDialog) {
        var idsSeleccionados by remember { mutableStateOf(viewModel.pacientesAsignadosIds.value.toSet()) }

        AlertDialog(
            onDismissRequest = { showAsignarDialog = false },
            title = { Text(text = "Seleccionar Pacientes", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(viewModel.todosLosPacientes.value) { paciente ->
                        val isSelected = idsSeleccionados.contains(paciente.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    idsSeleccionados = if (isSelected) idsSeleccionados - paciente.id else idsSeleccionados + paciente.id
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    idsSeleccionados = if (checked) idsSeleccionados + paciente.id else idsSeleccionados - paciente.id
                                },
                                colors = CheckboxDefaults.colors(checkedColor = tealButton)
                            )
                            Text(text = paciente.nombre.ifEmpty { "Paciente sin nombre" }, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.guardarAsignaciones(idsSeleccionados.toList())
                        showAsignarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tealButton)
                ) {
                    Text("Guardar Asignaciones", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAsignarDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // DIÁLOGO DE DETALLES DEL PACIENTE
    pacienteSeleccionado?.let { paciente ->
        AlertDialog(
            onDismissRequest = { pacienteSeleccionado = null },
            title = { Text(text = "Información del Paciente", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Nombre: ${paciente.nombre.ifEmpty { "N/A" }}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${paciente.email.ifEmpty { "N/A" }}")
                    Text("Edad: ${paciente.edad.ifEmpty { "N/A" }}")
                    Text("Tipo de Sangre: ${paciente.tipoSangre.ifEmpty { "N/A" }}")
                    Text("Emergencia: ${paciente.contactoEmergencia.ifEmpty { "N/A" }}")
                }
            },
            confirmButton = {
                TextButton(onClick = { pacienteSeleccionado = null }) {
                    Text("Cerrar", color = tealButton, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PacienteCard(nombre: String, tratamientos: String, estado: String, colorEstado: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
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