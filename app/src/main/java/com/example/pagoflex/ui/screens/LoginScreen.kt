package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.data.repository.AuthRepository
import com.example.pagoflex.viewmodel.LoginViewModel
import com.example.pagoflex.viewmodel.LoginViewModelFactory

@Composable
fun LoginScreen(
    onLoginExitoso: (String) -> Unit, // Recibe el rol (CLIENTE, ADMIN, etc.)
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(AuthRepository())
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Si el login es exitoso, navegamos a la siguiente pantalla
    LaunchedEffect(uiState.rolAutenticado) {
        if (uiState.rolAutenticado != null) {
            onLoginExitoso(uiState.rolAutenticado!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PagoFlex",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.correo,
            onValueChange = { viewModel.onCorreoChange(it) },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.correoError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.correoError != null) {
            Text(
                text = uiState.correoError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Clave
        OutlinedTextField(
            value = uiState.clave,
            onValueChange = { viewModel.onClaveChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.claveError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.claveError != null) {
            Text(
                text = uiState.claveError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.hacerLogin() },
            enabled = !uiState.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Iniciar Sesión")
            }
        }
        if (uiState.errorGeneral != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorGeneral!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}