package com.example.medicare.presentation.admin.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

// Importaciones de Navegación de Compose
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Importaciones de tus pantallas
import com.example.medicare.presentation.admin.historial.AdminHistorialScreen
import com.example.medicare.presentation.admin.monitoreo.MonitoreoScreen
import com.example.medicare.presentation.admin.pacientes.PacientesScreen
import com.example.medicare.presentation.admin.settings.AdminSettingsScreen
import com.example.medicare.presentation.admin.settings.AdminProfileScreen
import com.example.medicare.presentation.admin.settings.AdminNotificationsScreen
import com.example.medicare.presentation.admin.settings.AdminSecurityScreen

class AdminDashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    // 1. Inicializamos el controlador de rutas
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    // Detecta en qué pantalla estamos para pintar el ícono correspondiente
                    val currentRoute = navBackStackEntry?.destination?.route ?: "monitoreo"

                    val tealButton = Color(0xFF137A7F)

                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = if (currentRoute == "monitoreo") tealButton else Color.Gray) },
                                    label = { Text("Monitoreo", color = if (currentRoute == "monitoreo") tealButton else Color.Gray) },
                                    selected = currentRoute == "monitoreo",
                                    onClick = {
                                        navController.navigate("monitoreo") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.People, contentDescription = null, tint = if (currentRoute == "pacientes") tealButton else Color.Gray) },
                                    label = { Text("Pacientes", color = if (currentRoute == "pacientes") tealButton else Color.Gray) },
                                    selected = currentRoute == "pacientes",
                                    onClick = {
                                        navController.navigate("pacientes") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null, tint = if (currentRoute == "historial") tealButton else Color.Gray) },
                                    label = { Text("Historial", color = if (currentRoute == "historial") tealButton else Color.Gray) },
                                    selected = currentRoute == "historial",
                                    onClick = {
                                        navController.navigate("historial") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                val isSettingsActive = currentRoute in listOf("ajustes", "perfil", "notificaciones", "seguridad")
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = if (isSettingsActive) tealButton else Color.Gray) },
                                    label = { Text("Ajustes", color = if (isSettingsActive) tealButton else Color.Gray) },
                                    selected = isSettingsActive,
                                    onClick = {
                                        navController.navigate("ajustes") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            ResumenDiarioDialog()

                            NavHost(
                                navController = navController,
                                startDestination = "monitoreo"
                            ) {
                                composable("monitoreo") { MonitoreoScreen() }
                                composable("pacientes") { PacientesScreen() }
                                composable("historial") { AdminHistorialScreen() }
                                composable("ajustes") {
                                    AdminSettingsScreen(
                                        onNavigateToProfile = { navController.navigate("perfil") },
                                        onNavigateToNotifications = { navController.navigate("notificaciones") },
                                        onNavigateToSecurity = { navController.navigate("seguridad") }
                                    )
                                }
                                composable("perfil") { AdminProfileScreen(onBack = { navController.popBackStack() }) }
                                composable("notificaciones") { AdminNotificationsScreen(onBack = { navController.popBackStack() }) }
                                composable("seguridad") { AdminSecurityScreen(onBack = { navController.popBackStack() }) }
                            }
                        }
                    }
                }
            }
        }
    }
}