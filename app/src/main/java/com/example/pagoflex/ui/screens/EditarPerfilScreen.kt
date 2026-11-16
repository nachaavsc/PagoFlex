package com.example.pagoflex.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.pagoflex.ui.components.BannerConfirmacion
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.viewmodel.PerfilViewModel

@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val formularioActivo = !state.cargando && state.mensajeExito == null

    PantallaFormulario(titulo = "Editar perfil", onVolver = onVolver) {
        CampoTexto(
            valor = state.nombre,
            onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre completo",
            error = state.nombreError,
            habilitado = formularioActivo
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
            etiqueta = "Teléfono",
            error = state.telefonoError,
            tipoTeclado = KeyboardType.Phone,
            habilitado = formularioActivo
        )
        CampoTexto(
            valor = state.direccion,
            onValorChange = viewModel::onDireccionChange,
            etiqueta = "Dirección (opcional)",
            habilitado = formularioActivo
        )
        BannerConfirmacion(
            mensaje = state.mensajeExito,
            onOcultar = {
                viewModel.onMensajeMostrado()
                onVolver()
            }
        )
        BotonPrimario(
            texto = if (state.cargando) "Guardando..." else "Guardar cambios",
            onClick = viewModel::guardarPerfil,
            habilitado = formularioActivo
        )
    }
}
