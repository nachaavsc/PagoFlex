package com.example.pagoflex.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import com.example.pagoflex.ui.components.BannerConfirmacion
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.EfectoEventos
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.components.SelectorFotoPerfil
import com.example.pagoflex.ui.components.SnackbarPagoFlex
import com.example.pagoflex.ui.util.rememberVibrador
import com.example.pagoflex.viewmodel.EventoUI
import com.example.pagoflex.viewmodel.PerfilViewModel

@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val foto by viewModel.fotoPerfil.collectAsState()
    val formularioActivo = !state.cargando && state.mensajeExito == null
    val vibrador = rememberVibrador()
    val snackbarHostState = remember { SnackbarHostState() }

    EfectoEventos(viewModel.eventos, snackbarHostState) { evento ->
        if (evento is EventoUI.Exito) vibrador.confirmacion()
    }

    LaunchedEffect(state.mensajeExito) {
        if (state.mensajeExito != null) vibrador.confirmacion()
    }

    PantallaFormulario(
        titulo = "Editar perfil",
        onVolver = onVolver,
        snackbarHost = { SnackbarPagoFlex(snackbarHostState) }
    ) {
        SelectorFotoPerfil(
            foto = foto,
            onFotoTomada = viewModel::onFotoTomada,
            onImagenSeleccionada = viewModel::onImagenSeleccionada,
            onError = viewModel::onErrorFoto,
            habilitado = formularioActivo
        )
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
