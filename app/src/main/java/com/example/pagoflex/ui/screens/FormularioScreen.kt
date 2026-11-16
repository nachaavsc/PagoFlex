package com.example.pagoflex.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.theme.*
import com.example.pagoflex.viewmodel.FormularioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: FormularioViewModel,
    onNavegarHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            viewModel.resetGuardado()
            onNavegarHome()
        }
    }

    Scaffold(
        containerColor = FondoClaro,
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
                    containerColor = AzulCorporativo
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
                .verticalScroll(scrollState)
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

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoTexto(
                        valor = state.nombre,
                        onValorChange = viewModel::onNombreChange,
                        etiqueta = "Nombre del Destinatario",
                        error = state.nombreError
                    )

                    CampoTexto(
                        valor = state.correo,
                        onValorChange = viewModel::onCorreoChange,
                        etiqueta = "Correo Electrónico",
                        error = state.correoError,
                        tipoTeclado = KeyboardType.Email
                    )

                    CampoTexto(
                        valor = state.monto,
                        onValorChange = viewModel::onMontoChange,
                        etiqueta = "Monto ($)",
                        error = state.montoError,
                        tipoTeclado = KeyboardType.Number
                    )

                    CampoTexto(
                        valor = state.clave,
                        onValorChange = viewModel::onClaveChange,
                        etiqueta = "Clave de confirmación",
                        error = state.claveError,
                        esClave = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            BotonPrimario(
                texto = "Procesar Pago Flex",
                onClick = viewModel::guardarTransaccion
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
