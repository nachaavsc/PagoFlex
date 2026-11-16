package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.MensajeError
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.theme.AzulCorporativo
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.theme.TextoOscuro
import com.example.pagoflex.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onIngresoExitoso: (String) -> Unit,
    onCrearCuenta: () -> Unit,
    onRecuperarClave: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.rolAutenticado) {
        state.rolAutenticado?.let { rol ->
            viewModel.onNavegacionCompletada()
            onIngresoExitoso(rol)
        }
    }

    PantallaFormulario(
        titulo = null,
        onVolver = null,
        encabezado = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PagoFlex", color = AzulCorporativo, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
                Text("Tus compromisos de pago, siempre al día", color = TextoGris, fontSize = 14.sp)
            }
        },
        pie = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", color = TextoGris, fontSize = 14.sp)
                TextButton(onClick = onCrearCuenta) {
                    Text("Crear cuenta", color = AzulCorporativo, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) {
        Text("Iniciar sesión", color = TextoOscuro, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        CampoTexto(
            valor = state.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            error = state.correoError,
            tipoTeclado = KeyboardType.Email
        )
        CampoTexto(
            valor = state.clave,
            onValorChange = viewModel::onClaveChange,
            etiqueta = "Clave",
            error = state.claveError,
            esClave = true
        )
        MensajeError(state.errorGeneral)
        BotonPrimario(
            texto = if (state.cargando) "Ingresando..." else "Ingresar",
            onClick = viewModel::iniciarSesion,
            habilitado = !state.cargando
        )
        TextButton(onClick = onRecuperarClave, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("¿Olvidaste tu clave?", color = AzulCorporativo)
        }
    }
}
