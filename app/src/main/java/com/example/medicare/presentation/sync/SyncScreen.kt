package com.example.medicare.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicare.presentation.sync.SyncViewModel

@Composable
fun SyncScreen(
    syncViewModel: SyncViewModel = viewModel()
) {
    val pendientes = syncViewModel.pendientesDeSincronizar.value
    val isSyncing = syncViewModel.isSyncing.value
    val mensaje = syncViewModel.syncMessage.value

    val todoSincronizado = pendientes.isEmpty()
    val mainColor = if (todoSincronizado) Color(0xFF388E3C) else Color(0xFFE65100)
    val bgColor = Color(0xFFF5F6F8)

    LaunchedEffect(Unit) {
        syncViewModel.verificarPendientes()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(bgColor).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(mainColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (todoSincronizado) Icons.Default.CloudDone else Icons.Default.CloudSync,
                contentDescription = null,
                tint = mainColor,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (todoSincronizado) "Todo al día" else "Sincronización Pendiente",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mensaje,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { syncViewModel.sincronizarDatos() },
            enabled = !todoSincronizado && !isSyncing,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E6B9E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Subiendo datos...", fontSize = 16.sp)
            } else {
                Text(
                    text = if (todoSincronizado) "Base de datos actualizada" else "Sincronizar ahora",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}