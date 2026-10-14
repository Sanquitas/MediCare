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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
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
                    CaptureScreen(
                        ubicacion = ubicacionActual.value,
                        foto = fotoCapturada.value,
                        permisosListos = permisosConcedidos.value,
                        onRequestPermissions = {
                            requestPermissionsLauncher.launch(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
                            )
                        },
                        onTakePhoto = {
                            if (permisosConcedidos.value) {
                                takePictureLauncher.launch(null)
                            } else {
                                Toast.makeText(context, "Activa los permisos primero", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBack = { findNavController().popBackStack() },
                        onSave = {
                            Toast.makeText(context, "Toma guardada offline", Toast.LENGTH_SHORT).show()
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
                ubicacionActual.value = "Ubicación detectada (GPS): Av. Benito Juárez, Toluca, Mex."
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    ubicacion: String,
    foto: Bitmap?,
    permisosListos: Boolean,
    onRequestPermissions: () -> Unit,
    onTakePhoto: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val blueHeader = Color(0xFF1E6C93)
    val tealButton = Color(0xFF137A7F)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Toma", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(20.dp))
                        Text("MediCare", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = blueHeader)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Datos del medicamento
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Medicamento: Ibuprofeno", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Dosis: 1 Cápsula (02:00 PM)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Cámara", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Área de Cámara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray)
                    .clickable { onTakePhoto() },
                contentAlignment = Alignment.Center
            ) {
                if (foto != null) {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Presiona para capturar", color = Color.White)
                }

                // Botón flotante de cámara imitando el mockup
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (24).dp)
                        .size(48.dp)
                        .background(tealButton, CircleShape)
                        .border(4.dp, Color(0xFFF0F0F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Área de GPS
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (permisosListos) ubicacion else "Ubicación detectada (GPS): Av. Benito Juárez, Toluca, Mex.", fontSize = 12.sp, color = Color.DarkGray)                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF28A745), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubicación confirmada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF28A745))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permisos Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = permisosListos,
                    onCheckedChange = { if (it) onRequestPermissions() },
                    colors = SwitchDefaults.colors(checkedThumbColor = tealButton, checkedTrackColor = tealButton.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permiso de GPS y Cámara concedidos", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Final
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = tealButton),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Registrar Toma (Guardar offline)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}