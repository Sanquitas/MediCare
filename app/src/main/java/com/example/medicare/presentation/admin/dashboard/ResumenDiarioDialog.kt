package com.example.medicare.presentation.admin.dashboard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar // IMPORTACIÓN NECESARIA
import java.util.Date
import java.util.Locale

@Composable
fun ResumenDiarioDialog() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("MedicarePrefs", Context.MODE_PRIVATE)

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val lastShown = prefs.getString("last_summary_date", "")

    var showDialog by remember { mutableStateOf(false) }
    var tomasExitosas by remember { mutableStateOf(0) }
    var tomasPendientes by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (lastShown != today) {
            try {
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()
                val adminId = auth.currentUser?.uid

                if (adminId != null) {
                    val adminDoc = db.collection("usuarios").document(adminId).get().await()
                    val quiereResumen = adminDoc.getBoolean("notifResumen") ?: false
                    // 1. OBTENER LA LISTA DE PACIENTES ASIGNADOS AL ADMIN
                    val asignados = adminDoc.get("pacientesAsignados") as? List<String> ?: emptyList()

                    if (quiereResumen && asignados.isNotEmpty()) {

                        // 2. CALCULAR LOS LÍMITES DE TIEMPO DEL DÍA DE HOY
                        val calendar = Calendar.getInstance()
                        // Inicio del día (00:00:00)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val inicioDelDiaMillis = calendar.timeInMillis

                        // Fin del día (23:59:59)
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val finDelDiaMillis = calendar.timeInMillis - 1

                        val medsSnapshot = db.collectionGroup("medicamentos").get().await()
                        var exitosas = 0
                        var pendientes = 0

                        for (doc in medsSnapshot.documents) {
                            val userId = doc.reference.parent.parent?.id

                            // 3. SOLO PROCESAR MEDICAMENTOS DE PACIENTES ASIGNADOS
                            if (userId != null && asignados.contains(userId)) {
                                val tiempoDesbloqueo = doc.getLong("tiempoDesbloqueo") ?: 0L

                                // 4. SOLO PROCESAR MEDICAMENTOS DE HOY
                                if (tiempoDesbloqueo in inicioDelDiaMillis..finDelDiaMillis) {
                                    val estado = doc.getString("estado") ?: "Pendiente"
                                    if (estado == "Tomado") exitosas++
                                    else pendientes++
                                }
                            }
                        }

                        tomasExitosas = exitosas
                        tomasPendientes = pendientes
                        isLoading = false
                        showDialog = true

                        prefs.edit().putString("last_summary_date", today).apply()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MEDICARE", "Error cargando resumen: ${e.message}")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF137A7F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resumen Diario", fontWeight = FontWeight.Bold, color = Color(0xFF2C699A))
                }
            },
            text = {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF137A7F))
                } else {
                    Column {
                        Text("¡Buenos días! Aquí tienes el estado general de tus pacientes del día de hoy:", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("✅ Tomas exitosas hoy: $tomasExitosas", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("⚠️ Pendientes / Omitidas hoy: $tomasPendientes", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A7F))
                ) {
                    Text("Entendido", color = Color.White)
                }
            }
        )
    }
}