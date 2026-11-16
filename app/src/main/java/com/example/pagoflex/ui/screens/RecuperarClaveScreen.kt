package com.example.pagoflex.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BannerConfirmacion
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.MensajeError
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.viewmodel.RecuperarClaveViewModel

@Composable
fun RecuperarClaveScreen(
    viewModel: RecuperarClaveViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val formularioActivo = !state.cargando && state.mensajeExito == null

    PantallaFormulario(titulo = "Recuperar clave", onVolver = onVolver) {
        Text(
            "Verifica tu identidad con el correo y teléfono registrados para crear una nueva clave.",
            color = TextoGris,
            fontSize = 14.sp
        )
        CampoTexto(
            valor = state.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            error = state.correoError,
            tipoTeclado = KeyboardType.Email,
            habilitado = formularioActivo
        )
        CampoTexto(
            valor = state.telefono,
            onValorChange = viewModel::onTelefonoChange,
            etiqueta = "Teléfono registrado",
            error = state.telefonoError,
            tipoTeclado = KeyboardType.Phone,
            habilitado = formularioActivo
        )
        CampoTexto(
            valor = state.clave,
            onValorChange = viewModel::onClaveChange,
            etiqueta = "Nueva clave",
            error = state.claveError,
            esClave = true,
            habilitado = formularioActivo
        )
        CampoTexto(
            valor = state.confirmarClave,
            onValorChange = viewModel::onConfirmarClaveChange,
            etiqueta = "Confirmar nueva clave",
            error = state.confirmarClaveError,
            esClave = true,
            habilitado = formularioActivo
        )
        MensajeError(state.errorGeneral)
        BannerConfirmacion(
            mensaje = state.mensajeExito,
            onOcultar = {
                viewModel.onMensajeMostrado()
                onVolver()
            }
        )
        BotonPrimario(
            texto = if (state.cargando) "Actualizando..." else "Restablecer clave",
            onClick = viewModel::restablecerClave,
            habilitado = formularioActivo
        )
    }
}
