package com.example.medicare.presentation.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.medicare.R
import com.example.medicare.domain.models.Medicamento
import com.example.medicare.presentation.profile.AjustesScreen
import com.example.medicare.presentation.profile.ProfileViewModel
import com.example.medicare.presentation.profile.ProfileScreen

class PatientDashboardFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val viewModel: PatientViewModel = viewModel()
                    val profileViewModel: ProfileViewModel = viewModel()

                    val perfil = viewModel.patientData.value
                    val medicamentos = viewModel.medicamentosDelDia
                    val isLoading = viewModel.isLoading.value

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
                            when (selectedTab) {
                                0 -> InicioScreen(
                                    nombrePaciente = perfil.nombre,
                                    medicamentos = medicamentos,
                                    isLoading = isLoading,
                                    onNavigateToCapture = { medicamentoId ->
                                        val bundle = Bundle().apply { putString("MEDICAMENTO_ID", medicamentoId) }
                                        findNavController().navigate(R.id.action_patient_to_capture, bundle)
                                    },
                                    onAgregarMedicamento = { nombre, dosis, hora, frecuencia, cantidad ->
                                        viewModel.agregarMedicamento(nombre, dosis, hora, frecuencia, cantidad)
                                    },
                                    verificarHora = { tiempoDesbloqueo -> System.currentTimeMillis() >= tiempoDesbloqueo },
                                    onOmitirMedicamento = { id -> viewModel.omitirMedicamento(id) }
                                )
                                1 -> HistorialScreen(medicamentos = medicamentos)
                                2 -> ProfileScreen(
                                    viewModel = profileViewModel,
                                    onNavigateBack = { selectedTab = 0 },
                                    onLogout = {
                                        findNavController().navigate(R.id.action_login_to_patient)
                                        findNavController().popBackStack()
                                    },
                                    onTratamientosClick = { selectedTab = 0 },
                                    onAjustesClick = { selectedTab = 4 },
                                    onSyncClick = { selectedTab = 3 }
                                )
                                3 -> SyncScreen()
                                4 -> AjustesScreen(onNavigateBack = { selectedTab = 2 })
                            }
                        }
                    }
                }
            }
        }
    }
}