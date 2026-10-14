package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen() {
    val blueHeader = Color(0xFF2C699A)
    val bgGray = Color(0xFFF5F7F8)

    Column(modifier = Modifier.fillMaxSize().background(bgGray).verticalScroll(rememberScrollState())) {
        // TopBar
        TopAppBar(
            title = { Text("Historial", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
            },
            actions = {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
        )

        // Fechas
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            DateItem("Lun,\n12\nOct", isSelected = true)
            DateItem("Mar,\n13\nOct", isSelected = false)
            DateItem("Mié,\n14\nOct", isSelected = false)
            DateItem("Jue,\n15\nOct", isSelected = false)
            DateItem("Vie,\n16\nOct", isSelected = false)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Buscador
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = "", onValueChange = {},
                    placeholder = { Text("Buscar medicamento...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFEEEEEE), unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(50.dp).background(Color(0xFF137A7F), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registros
            HistoryCard("Paracetamol - 08:00 AM", "Tomado", Color(0xFF28A745), Icons.Default.Check, "08:03 AM", true)
            HistoryCard("Ibuprofeno - 02:00 PM", "Retrasado", Color(0xFFFFA000), Icons.Default.Check, "02:45 PM", false)
            HistoryCard("Complejo B - 09:00 PM", "Omitido", Color(0xFFDC3545), Icons.Default.Close, "", false)
            HistoryCard("Aspirina - 08:00 AM (Ayer)", "Tomado", Color(0xFF28A745), Icons.Default.Check, "08:05 AM", true)
        }
    }
}

@Composable
fun DateItem(text: String, isSelected: Boolean) {
    val bg = if (isSelected) Color(0xFF2C699A) else Color(0xFFEEEEEE)
    val textCol = if (isSelected) Color.White else Color.Black
    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = textCol, fontSize = 14.sp, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun HistoryCard(title: String, status: String, statusColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, time: String, hasCamera: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(statusColor, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(status, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (time.isNotEmpty()) Text(time, fontSize = 14.sp, color = Color.DarkGray)
                    if (hasCamera) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF137A7F), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}