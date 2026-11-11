package com.example.medicare.presentation.admin.monitoreo

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicare.domain.models.Medicamento

data class PacienteOpcion(
    val id: String,
    val nombre: String
)

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

    var showAddDialog by remember { mutableStateOf(false) }
    var pacienteAMensajear by remember { mutableStateOf<PacienteMonitoreo?>(null) }
    val context = LocalContext.current

    val opcionesPacientes = remember(pacientesMonitoreo) {
        pacientesMonitoreo.map { PacienteOpcion(it.pacienteId, it.nombrePaciente) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Text("No tienes pacientes asignados. Asigna pacientes desde la sección 'Pacientes'.", color = Color.Gray)
                    }
                } else {
                    pacientesMonitoreo.forEach { paciente ->
                        val totalTomas = paciente.medicamentos.size
                        val tomasRealizadas = paciente.medicamentos.count { it.estado == "Tomado" }
                        val progreso = if (totalTomas > 0) tomasRealizadas.toFloat() / totalTomas.toFloat() else 0f

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
                                    if (totalTomas > 0) {
                                        Text("Historial de hoy: $tomasRealizadas tomadas de $totalTomas tomas finalizadas", fontSize = 12.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progreso },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)),
                                            color = tealButton,
                                            trackColor = Color(0xFFE0E0E0)
                                        )
                                    } else {
                                        Text("Sin tomas registradas (Tomadas u Omitidas) aún", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    val omisiones = paciente.omisionesRecientes.size
                                    if (omisiones >= MonitoreoViewModel.UMBRAL_OMISIONES) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFFF8D7DA), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF721C24), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Omitió $omisiones tomas en los últimos ${MonitoreoViewModel.DIAS_REVISION} días",
                                                fontSize = 12.sp,
                                                color = Color(0xFF721C24),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { pacienteAMensajear = paciente },
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, contentDescription = null, tint = tealButton, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Enviar mensaje", fontSize = 12.sp, color = tealButton)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        paciente.medicamentos.forEach { medicamento ->
                            RegistroCard(medicamento = medicamento, tealButton = tealButton)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        if (opcionesPacientes.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = tealButton,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar medicamento a paciente")
            }
        }
    }

    if (showAddDialog && opcionesPacientes.isNotEmpty()) {
        AddMedicationDialogForAdmin(
            pacientes = opcionesPacientes,
            onDismiss = { showAddDialog = false },
            onConfirm = { pacienteId, nombre, dosis, hora, frecuencia, cantidad, onError ->
                viewModel.agregarMedicamentoAPaciente(
                    pacienteId = pacienteId,
                    nombre = nombre,
                    dosis = dosis,
                    hora = hora,
                    frecuencia = frecuencia,
                    cantidadTotal = cantidad,
                    onSuccess = { showAddDialog = false },
                    onError = onError
                )
            }
        )
    }

    pacienteAMensajear?.let { paciente ->
        EnviarMensajeDialog(
            nombrePaciente = paciente.nombrePaciente,
            textoInicial = viewModel.generarMensajeSugerido(paciente),
            onDismiss = { pacienteAMensajear = null },
            onConfirm = { texto, onError ->
                viewModel.enviarMensajeAPaciente(
                    pacienteId = paciente.pacienteId,
                    texto = texto,
                    onSuccess = {
                        pacienteAMensajear = null
                        Toast.makeText(context, "Mensaje enviado a ${paciente.nombrePaciente}", Toast.LENGTH_SHORT).show()
                    },
                    onError = onError
                )
            }
        )
    }
}

@Composable
fun EnviarMensajeDialog(
    nombrePaciente: String,
    textoInicial: String,
    onDismiss: () -> Unit,
    onConfirm: (texto: String, onError: (String) -> Unit) -> Unit
) {
    var texto by remember { mutableStateOf(textoInicial) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enviando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mensaje para $nombrePaciente", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Mensaje") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (texto.isBlank()) {
                        errorMessage = "El mensaje no puede estar vacío."
                    } else {
                        enviando = true
                        errorMessage = null
                        onConfirm(texto) { errorMsg ->
                            enviando = false
                            errorMessage = errorMsg
                        }
                    }
                },
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A7F))
            ) {
                Text(if (enviando) "Enviando..." else "Enviar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialogForAdmin(
    pacientes: List<PacienteOpcion>,
    pacienteInicialId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (pacienteId: String, nombre: String, dosis: String, hora: String, frecuencia: String, cantidadTotal: Int, onError: (String) -> Unit) -> Unit
) {
    var pacienteSeleccionadoId by remember { mutableStateOf(pacienteInicialId ?: pacientes.firstOrNull()?.id ?: "") }
    var pacienteSeleccionadoNombre by remember {
        mutableStateOf(pacientes.find { it.id == pacienteSeleccionadoId }?.nombre ?: "")
    }

    var expandedPaciente by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("") }
    var cantidadTotal by remember { mutableStateOf("") }

    var expandedFrecuencia by remember { mutableStateOf(false) }
    val opcionesFrecuencia = listOf(
        "Cada 4 horas",
        "Cada 6 horas",
        "Cada 8 horas",
        "Cada 12 horas",
        "Cada 24 horas (Una vez al día)"
    )

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Medicamento para Paciente", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ExposedDropdownMenuBox(
                    expanded = expandedPaciente,
                    onExpandedChange = { expandedPaciente = !expandedPaciente; showError = false }
                ) {
                    OutlinedTextField(
                        value = pacienteSeleccionadoNombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paciente asignado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaciente) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPaciente,
                        onDismissRequest = { expandedPaciente = false }
                    ) {
                        pacientes.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.nombre) },
                                onClick = {
                                    pacienteSeleccionadoId = p.id
                                    pacienteSeleccionadoNombre = p.nombre
                                    expandedPaciente = false
                                    showError = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; showError = false },
                    label = { Text("Nombre (ej. Paracetamol)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dosis,
                    onValueChange = { dosis = it; showError = false },
                    label = { Text("Dosis (ej. 1 tableta)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it; showError = false },
                    label = { Text("Hora planeada (ej. 08:00 AM)") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = cantidadTotal.toIntOrNull() ?: 0
                    val horaRegex = Regex("^(0?[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM|am|pm)$")

                    when {
                        pacienteSeleccionadoId.isBlank() -> {
                            errorMessage = "Debes seleccionar un paciente."
                            showError = true
                        }
                        nombre.trim().length < 3 -> {
                            errorMessage = "El nombre debe tener al menos 3 letras."
                            showError = true
                        }
                        dosis.trim().isEmpty() -> {
                            errorMessage = "La dosis no puede estar vacía."
                            showError = true
                        }
                        !hora.trim().matches(horaRegex) -> {
                            errorMessage = "Formato de hora inválido. Usa ej: 08:00 AM"
                            showError = true
                        }
                        frecuencia.isEmpty() -> {
                            errorMessage = "Debes seleccionar una frecuencia."
                            showError = true
                        }
                        total <= 0 -> {
                            errorMessage = "El total de tomas debe ser mayor a 0."
                            showError = true
                        }
                        else -> {
                            showError = false
                            onConfirm(
                                pacienteSeleccionadoId,
                                nombre.trim(),
                                dosis.trim(),
                                hora.trim().uppercase(),
                                frecuencia,
                                total
                            ) { errorMsg ->
                                errorMessage = errorMsg
                                showError = true
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A7F))
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun RegistroCard(medicamento: Medicamento, tealButton: Color) {
    val isTomado = medicamento.estado == "Tomado"
    val isOmitido = medicamento.estado == "Omitido"

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
                    if (imagenDecodificada != null) {
                        Image(
                            bitmap = imagenDecodificada,
                            contentDescription = "Evidencia fotográfica",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
                            onClick = { },
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
