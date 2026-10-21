package com.example.medicare.presentation.capture

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class CaptureFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var ubicacionActual = mutableStateOf("Buscando GPS...")
    private var fotoCapturada = mutableStateOf<Bitmap?>(null)
    private var permisosConcedidos = mutableStateOf(false)

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val gpsGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val camGranted = permissions[Manifest.permission.CAMERA] == true
        if (gpsGranted && camGranted) {
            permisosConcedidos.value = true
            obtenerUbicacion()
        } else {
            permisosConcedidos.value = false
            Toast.makeText(context, "Se necesitan ambos permisos", Toast.LENGTH_LONG).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        fotoCapturada.value = bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    // 1. Instanciamos el ViewModel
                    val captureViewModel: CaptureViewModel = viewModel()

                    // 2. Recuperamos el ID del medicamento que nos envió InicioScreen
                    val medicamentoId = arguments?.getString("MEDICAMENTO_ID") ?: ""

                    CaptureScreen(
                        viewModel = captureViewModel,
                        medicamentoId = medicamentoId,
                        ubicacion = ubicacionActual.value,
                        foto = fotoCapturada.value,
                        permisosListos = permisosConcedidos.value,
                        onRequestPermissions = {
                            requestPermissionsLauncher.launch(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
                            )
                        },
                        onTakePhoto = {
                            if (permisosConcedidos.value) takePictureLauncher.launch(null)
                            else Toast.makeText(context, "Activa los permisos primero", Toast.LENGTH_SHORT).show()
                        },
                        onBack = { findNavController().popBackStack() },
                        onSaveSuccess = {
                            Toast.makeText(context, "Toma confirmada exitosamente", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                ubicacionActual.value = "Lat: ${String.format("%.4f", location.latitude)}, Lon: ${String.format("%.4f", location.longitude)}"
            } else {
                ubicacionActual.value = "GPS inactivo, enciende tu ubicación"
            }
        }.addOnFailureListener {
            ubicacionActual.value = "Error al obtener GPS"
        }
    }
}