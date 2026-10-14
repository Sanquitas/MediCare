package com.example.medicare.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class AdminDashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    var selectedTab by remember { mutableStateOf(0) }
                    val tealButton = Color(0xFF137A7F)

                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = if(selectedTab == 0) tealButton else Color.Gray) },
                                    label = { Text("Monitoreo", color = if(selectedTab == 0) tealButton else Color.Gray) },
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.People, contentDescription = null, tint = if(selectedTab == 1) tealButton else Color.Gray) },
                                    label = { Text("Pacientes", color = if(selectedTab == 1) tealButton else Color.Gray) },
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null, tint = if(selectedTab == 2) tealButton else Color.Gray) },
                                    label = { Text("Historial", color = if(selectedTab == 2) tealButton else Color.Gray) },
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = if(selectedTab == 3) tealButton else Color.Gray) },
                                    label = { Text("Ajustes", color = if(selectedTab == 3) tealButton else Color.Gray) },
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            when (selectedTab) {
                                0 -> MonitoreoScreen()
                                1 -> PacientesScreen()
                                2 -> AdminHistorialScreen()
                                3 -> AdminSettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}