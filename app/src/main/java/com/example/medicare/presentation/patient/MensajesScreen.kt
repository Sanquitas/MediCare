package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.medicare.domain.models.MensajeCuidador
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MensajesScreen(
    mensajes: List<MensajeCuidador>,
    onMarcarComoLeidos: () -> Unit
) {
    val blueHeader = Color(0xFF2C699A)
    val backgroundLight = Color(0xFFF5F6F8)

    // Recordamos cuáles eran nuevos al abrir la pestaña para seguir resaltándolos aunque ya se marquen como leídos
    val idsNuevos = remember { mutableSetOf<String>() }
    val noLeidos = mensajes.filter { !it.leido }
    noLeidos.forEach { idsNuevos.add(it.id) }

    // Al abrir la pestaña (o al llegar un mensaje nuevo con la pestaña abierta) se marcan como leídos
    LaunchedEffect(noLeidos.size) {
        onMarcarComoLeidos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(blueHeader, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Mensajes de tu cuidador", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        if (mensajes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Aún no tienes mensajes.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mensajes, key = { it.id }) { mensaje ->
                    MensajeCard(mensaje = mensaje, esNuevo = mensaje.id in idsNuevos, colorAcento = blueHeader)
                }
            }
        }
    }
}

@Composable
private fun MensajeCard(mensaje: MensajeCuidador, esNuevo: Boolean, colorAcento: Color) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "ES")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (esNuevo) colorAcento else Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = colorAcento, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    mensaje.remitenteNombre.ifEmpty { "Tu cuidador" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                if (esNuevo) {
                    Text("Nuevo", fontSize = 11.sp, color = colorAcento, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).background(colorAcento, CircleShape))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(mensaje.texto, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(sdf.format(Date(mensaje.fecha)), fontSize = 11.sp, color = Color.Gray)
        }
    }
}
