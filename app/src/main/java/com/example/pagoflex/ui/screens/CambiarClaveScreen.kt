package com.example.pagoflex.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BannerConfirmacion
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.util.rememberVibrador
import com.example.pagoflex.viewmodel.CambiarClaveViewModel

@Composable
fun CambiarClaveScreen(
    viewModel: CambiarClaveViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val formularioActivo = !state.cargando && state.mensajeExito == null
    val vibrador = rememberVibrador()

    LaunchedEffect(state.mensajeExito) {
        if (state.mensajeExito != null) vibrador.confirmacion()
    }

    PantallaFormulario(titulo = "Cambiar clave", onVolver = onVolver) {
        CampoTexto(
            valor = state.claveActual,
            onValorChange = viewModel::onClaveActualChange,
            etiqueta = "Clave actual",
            error = state.claveActualError,
            esClave = true,
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
        Text(
            "Mínimo 8 caracteres, una mayúscula, un número y un carácter especial.",
            color = TextoGris,
            fontSize = 12.sp
        )
        BannerConfirmacion(
            mensaje = state.mensajeExito,
            onOcultar = {
                viewModel.onMensajeMostrado()
                onVolver()
            }
        )
        BotonPrimario(
            texto = if (state.cargando) "Actualizando..." else "Actualizar clave",
            onClick = viewModel::cambiarClave,
            habilitado = formularioActivo
        )
    }
}
