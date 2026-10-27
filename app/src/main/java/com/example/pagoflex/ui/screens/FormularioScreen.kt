package com.example.pagoflex.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.viewmodel.FormularioViewModel

private val FondoMoradoSuave = Color(0xFFF3E8FF)
private val MoradoPrincipal = Color(0xFF9333EA)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeLila = Color(0xFFE9D5FF)
private val TextoOscuro = Color(0xFF3B0764)
private val TextoGris = Color(0xFF6B21A8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: FormularioViewModel,
    onNavegarHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.resetGuardado()
            onNavegarHome()
        }
    }

    Scaffold(
        containerColor = FondoMoradoSuave,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo Pago Flex",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MoradoPrincipal
                ),
                navigationIcon = {
                    TextButton(onClick = onNavegarHome) {
                        Text("Volver", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa los datos para registrar un nuevo pago",
                color = TextoGris,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BordeLila, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre del Destinatario") },
                        isError = state.nombreError != null,
                        supportingText = {
                            state.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoradoPrincipal,
                            focusedLabelColor = MoradoPrincipal,
                            unfocusedBorderColor = BordeLila
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.correo,
                        onValueChange = { viewModel.onCorreoChange(it) },
                        label = { Text("Correo Electrónico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = state.correoError != null,
                        supportingText = {
                            state.correoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoradoPrincipal,
                            focusedLabelColor = MoradoPrincipal,
                            unfocusedBorderColor = BordeLila
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.monto,
                        onValueChange = { viewModel.onMontoChange(it) },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.montoError != null,
                        supportingText = {
                            state.montoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoradoPrincipal,
                            focusedLabelColor = MoradoPrincipal,
                            unfocusedBorderColor = BordeLila
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña de confirmación") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoradoPrincipal,
                            focusedLabelColor = MoradoPrincipal,
                            unfocusedBorderColor = BordeLila
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = state.esValido,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = { viewModel.guardarTransaccion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoradoPrincipal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Procesar Pago Flex",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}