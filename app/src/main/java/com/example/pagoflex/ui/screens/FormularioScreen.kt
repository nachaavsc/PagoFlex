package com.example.pagoflex.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.viewmodel.FormularioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    onNavegarHome: () -> Unit,
    viewModel: FormularioViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Redirige al inicio cuando se completa el guardado en la base de datos (Room)
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.resetGuardado()
            onNavegarHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Pago Flex") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo Nombre
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text("Nombre del Destinatario") },
                isError = state.nombreError != null,
                supportingText = {
                    state.nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Correo Electrónico
            OutlinedTextField(
                value = state.correo,
                onValueChange = { viewModel.onCorreoChange(it) },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.correoError != null,
                supportingText = {
                    state.correoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Monto
            OutlinedTextField(
                value = state.monto,
                onValueChange = { viewModel.onMontoChange(it) },
                label = { Text("Monto ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.montoError != null,
                supportingText = {
                    state.montoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animación de aparición suave cuando el formulario es válido
            AnimatedVisibility(
                visible = state.esValido,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = { viewModel.guardarTransaccion() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Procesar Pago Flex")
                }
            }
        }
    }
}
