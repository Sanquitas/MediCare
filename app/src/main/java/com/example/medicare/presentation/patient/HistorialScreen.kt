package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicare.domain.models.Medicamento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    medicamentos: List<Medicamento>
) {
    val blueHeader = Color(0xFF3972AC)
    val tealButton = Color(0xFF217376)
    val backgroundLight = Color(0xFFF5F6F8)

    var searchQuery by remember { mutableStateOf("") }

    // 1. GENERAMOS LOS DÍAS REALES DEL CALENDARIO (Últimos 7 días)
    val diasReales = remember {
        val list = mutableListOf<Triple<String, String, String>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6) // Retrocedemos 6 días para llegar a hoy

        val sdfDiaSemana = SimpleDateFormat("EEE,", Locale("es", "ES"))
        val sdfNum = SimpleDateFormat("dd", Locale("es", "ES"))
        val sdfMes = SimpleDateFormat("MMM", Locale("es", "ES"))

        for (i in 0..6) {
            val d1 = sdfDiaSemana.format(cal.time).replaceFirstChar { it.uppercase() }
            val d2 = sdfNum.format(cal.time)
            val d3 = sdfMes.format(cal.time).replaceFirstChar { it.uppercase() }
            list.add(Triple(d1, d2, d3))
            cal.add(Calendar.DAY_OF_YEAR, 1) // Avanzamos un día
        }
        list
    }

    // Por defecto, seleccionamos el último día (que siempre será "Hoy")
    var selectedDateIndex by remember { mutableStateOf(diasReales.size - 1) }

    // Auto-scroll del calendario para que siempre se vea "Hoy" al entrar
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = diasReales.size - 1)

    // 2. LÓGICA DEL FILTRO DE FECHAS
    val diaSeleccionado = diasReales[selectedDateIndex]
    val fechaFiltro = "${diaSeleccionado.second} ${diaSeleccionado.third}" // Ejemplo: "19 Oct"

    val medicamentosFiltrados = medicamentos.filter { med ->
        // Convertimos el tiempoDesbloqueo (Milisegundos) a texto ("19 Oct")
        val fechaMed = if (med.tiempoDesbloqueo > 0L) {
            SimpleDateFormat("dd MMM", Locale("es", "ES")).format(Date(med.tiempoDesbloqueo))
        } else {
            "" // Manejo para pastillas antiguas de prueba creadas antes de usar Long
        }

        val coincideTexto = med.nombre.contains(searchQuery, ignoreCase = true)
        val estaTerminado = med.estado != "Pendiente"

        // Comparamos si la fecha de la pastilla es igual a la fecha seleccionada en los botones
        // (Si el tiempoDesbloqueo es 0, lo mostramos siempre para no ocultar tus datos de prueba viejos)
        val coincideFecha = fechaMed.equals(fechaFiltro, ignoreCase = true) || med.tiempoDesbloqueo == 0L

        coincideTexto && estaTerminado && coincideFecha
    }

    Column(modifier = Modifier.fillMaxSize().background(backgroundLight)) {
        // TopBar
        Row(
            modifier = Modifier.fillMaxWidth().background(blueHeader).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
                Text("MediCare", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text("Historial", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = Color.White, modifier = Modifier.size(24.dp))
        }

        // Carrusel Dinámico de Fechas
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(diasReales.size) { index ->
                val isSelected = selectedDateIndex == index
                val dayInfo = diasReales[index]

                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) blueHeader else Color(0xFFE4E9ED))
                        .clickable { selectedDateIndex = index } // ¡ACTUALIZA EL FILTRO AL TOCAR!
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(dayInfo.first, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp)
                    Text(dayInfo.second, color = if (isSelected) Color.White else Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(dayInfo.third, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp)
                }
            }
        }

        // Barra de búsqueda
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar medicamento...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFEBEBEB),
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = tealButton
                ),
                modifier = Modifier.weight(1f).height(52.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(tealButton).clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista Filtrada
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            if (medicamentosFiltrados.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay registros en esta fecha.", color = Color.Gray, fontSize = 15.sp)
                    }
                }
            } else {
                items(medicamentosFiltrados.size) { index ->
                    HistorialCard(medicamentosFiltrados[index])
                }
            }
        }
    }
}

@Composable
fun HistorialCard(med: Medicamento) {
    val badgeColor = when (med.estado) {
        "Tomado" -> Color(0xFF388E3C)
        "Retrasado" -> Color(0xFFF6B23C)
        "Omitido" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    val iconVector = when (med.estado) {
        "Omitido" -> Icons.Default.Close
        else -> Icons.Default.Check
    }

    val rightIcon = when(med.estado) {
        "Tomado" -> Icons.Default.CameraAlt
        "Retrasado" -> Icons.Default.Check
        else -> null
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("${med.nombre} - ${med.hora}", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF333333))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeColor).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(med.estado, color = if(med.estado == "Retrasado") Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(iconVector, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                }

                if (med.estado != "Omitido") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (med.horaToma.isNotEmpty()) med.horaToma else med.hora, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (rightIcon != null) {
                            Icon(imageVector = rightIcon, contentDescription = null, tint = Color(0xFF217376), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}