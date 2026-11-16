package com.example.pagoflex.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onRegistroExitoso: (String) -> Unit,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.rolAutenticado) {
        state.rolAutenticado?.let { rol ->
            viewModel.onNavegacionCompletada()
            onRegistroExitoso(rol)
        }
    }

    PantallaFormulario(titulo = "Crear cuenta", onVolver = onVolver) {
        Text(
            "Completa tus datos para empezar a gestionar tus pagos.",
            color = TextoGris,
            fontSize = 14.sp
        )
        CampoTexto(
            valor = state.nombre,
            onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre completo",
            error = state.nombreError
        )
        CampoTexto(
            valor = state.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            error = state.correoError,
            tipoTeclado = KeyboardType.Email
        )
        CampoTexto(
            valor = state.telefono,
            onValorChange = viewModel::onTelefonoChange,
            etiqueta = "Teléfono",
            error = state.telefonoError,
            tipoTeclado = KeyboardType.Phone
        )
        CampoTexto(
            valor = state.clave,
            onValorChange = viewModel::onClaveChange,
            etiqueta = "Clave",
            error = state.claveError,
            esClave = true
        )
        CampoTexto(
            valor = state.confirmarClave,
            onValorChange = viewModel::onConfirmarClaveChange,
            etiqueta = "Confirmar clave",
            error = state.confirmarClaveError,
            esClave = true
        )
        Text(
            "Mínimo 8 caracteres, una mayúscula, un número y un carácter especial.",
            color = TextoGris,
            fontSize = 12.sp
        )
        BotonPrimario(
            texto = if (state.cargando) "Creando cuenta..." else "Crear cuenta",
            onClick = viewModel::crearCuenta,
            habilitado = !state.cargando
        )
    }
}
