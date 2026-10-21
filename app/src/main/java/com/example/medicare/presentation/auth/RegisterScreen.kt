package com.example.medicare.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("paciente") }

    // Estados para los campos médicos
    var edad by remember { mutableStateOf("") }
    var tipoSangre by remember { mutableStateOf("") }
    var contactoEmergencia by remember { mutableStateOf("") }

    // Estados para expandir/contraer los menús desplegables
    var expandedEdad by remember { mutableStateOf(false) }
    var expandedSangre by remember { mutableStateOf(false) }

    // Listas de opciones
    val opcionesSangre = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Desconocido")
    val opcionesEdad = (1..110).map { it.toString() } // Genera una lista del "1" al "110"

    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D5C75), Color(0xFF1E824C))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.20f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Crear Cuenta",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regístrate en MediCare",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.80f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0D5C75)) },
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D5C75),
                        unfocusedBorderColor = Color(0xFF8BAEBD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0D5C75)) },
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D5C75),
                        unfocusedBorderColor = Color(0xFF8BAEBD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0D5C75)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D5C75),
                        unfocusedBorderColor = Color(0xFF8BAEBD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Menú desplegable para la EDAD
                ExposedDropdownMenuBox(
                    expanded = expandedEdad,
                    onExpandedChange = { expandedEdad = !expandedEdad }
                ) {
                    OutlinedTextField(
                        value = edad,
                        onValueChange = { },
                        readOnly = true, // Evita que escriban texto libre
                        label = { Text("Edad") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF0D5C75)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEdad) },
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D5C75),
                            unfocusedBorderColor = Color(0xFF8BAEBD)
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEdad,
                        onDismissRequest = { expandedEdad = false }
                    ) {
                        opcionesEdad.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    edad = seleccion
                                    expandedEdad = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Menú desplegable para el TIPO DE SANGRE
                ExposedDropdownMenuBox(
                    expanded = expandedSangre,
                    onExpandedChange = { expandedSangre = !expandedSangre }
                ) {
                    OutlinedTextField(
                        value = tipoSangre,
                        onValueChange = { },
                        readOnly = true, // Evita que escriban texto libre
                        label = { Text("Tipo de Sangre") },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF0D5C75)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSangre) },
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D5C75),
                            unfocusedBorderColor = Color(0xFF8BAEBD)
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSangre,
                        onDismissRequest = { expandedSangre = false }
                    ) {
                        opcionesSangre.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    tipoSangre = seleccion
                                    expandedSangre = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contactoEmergencia,
                    onValueChange = { contactoEmergencia = it },
                    label = { Text("Contacto de Emergencia") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0D5C75)) },
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D5C75),
                        unfocusedBorderColor = Color(0xFF8BAEBD)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Selecciona tu rol:", fontWeight = FontWeight.Bold, color = Color(0xFF0D5C75))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedRole == "paciente"),
                            onClick = { selectedRole = "paciente" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF137A7F))
                        )
                        Text("Paciente", color = Color.DarkGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedRole == "admin"),
                            onClick = { selectedRole = "admin" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF137A7F))
                        )
                        Text("Administrador", color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF137A7F))
                } else {
                    Button(
                        onClick = {
                            // ESTANDARIZAMOS: Siempre guardamos en minúsculas exactas para evitar fallos
                            val rolFormateado = if (selectedRole == "paciente") "paciente" else "admin"

                            viewModel.register(
                                email = email,
                                pass = password,
                                nombre = nombre,
                                rol = rolFormateado, // Guarda "paciente" o "admin" de forma limpia
                                tipoSangre = tipoSangre,
                                edad = edad,
                                contactoEmergencia = contactoEmergencia,
                                onSuccess = onRegisterSuccess
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137A7F)),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrarse", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onBackToLogin) {
                        Text("¿Ya tienes cuenta? Inicia Sesión", color = Color(0xFF0D5C75))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}