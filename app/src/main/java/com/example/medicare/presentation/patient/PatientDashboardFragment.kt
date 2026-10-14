package com.example.medicare.presentation.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicare.R

class PatientDashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    // Estado para controlar qué pestaña está activa (0=Inicio, 1=Historial, 2=Perfil, 3=Sincronizar)
                    var selectedTab by remember { mutableStateOf(0) }

                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = if(selectedTab == 0) Color(0xFF2C699A) else Color.Gray) },
                                    label = { Text("Inicio", color = if(selectedTab == 0) Color(0xFF2C699A) else Color.Gray) },
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null, tint = if(selectedTab == 1) Color(0xFF2C699A) else Color.Gray) },
                                    label = { Text("Historial", color = if(selectedTab == 1) Color(0xFF2C699A) else Color.Gray) },
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = if(selectedTab == 2) Color(0xFF2C699A) else Color.Gray) },
                                    label = { Text("Mi Perfil", color = if(selectedTab == 2) Color(0xFF2C699A) else Color.Gray) },
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Sync, contentDescription = null, tint = if(selectedTab == 3) Color(0xFF2C699A) else Color.Gray) },
                                    label = { Text("Sincronizar", color = if(selectedTab == 3) Color(0xFF2C699A) else Color.Gray) },
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            // Intercambiador de Vistas
                            when (selectedTab) {
                                0 -> InicioScreen { findNavController().navigate(R.id.action_patient_to_capture) }
                                1 -> HistorialScreen()
                                2 -> ProfileScreen()
                                3 -> SyncScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extraímos el "Inicio" a su propio Composable
@Composable
fun InicioScreen(onNavigateToCapture: () -> Unit) {
    val blueHeader = Color(0xFF2C699A)
    val greenOffline = Color(0xFF4CAF50)
    val tealButton = Color(0xFF137A7F)

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
            Text("Modo Local (offline)", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lunes, 12 de Octubre", color = Color.Gray, fontSize = 14.sp)
            Text("¡Hola, Jesús!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EDDA)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Paracetamol", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("08:00 AM • 1 Tableta", fontSize = 14.sp)
                        Text("Día 3 de 7", fontSize = 12.sp, color = Color.DarkGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF28A745), modifier = Modifier.size(32.dp))
                        Text("Tomado", color = Color(0xFF28A745), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(1.dp, blueHeader, RoundedCornerShape(12.dp))) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Ibuprofeno", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("02:00 PM • 1 Cápsula", fontSize = 14.sp)
                        Text("Faltan 5 días", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(onClick = onNavigateToCapture, colors = ButtonDefaults.buttonColors(containerColor = tealButton), shape = RoundedCornerShape(20.dp)) {
                        Text("Confirmar Toma")
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD0E2FF)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Complejo B", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("09:00 PM • 1 Tableta", fontSize = 14.sp)
                        Text("Próxima toma", fontSize = 12.sp, color = Color.DarkGray)
                    }
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = blueHeader, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}