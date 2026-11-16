package com.example.pagoflex.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoFecha
import com.example.pagoflex.ui.components.CampoSeleccion
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.EfectoEventos
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.components.SnackbarPagoFlex
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.theme.TextoOscuro
import com.example.pagoflex.ui.util.rememberVibrador
import com.example.pagoflex.viewmodel.EventoUI
import com.example.pagoflex.viewmodel.NuevoCompromisoViewModel

@Composable
fun NuevoCompromisoScreen(
    viewModel: NuevoCompromisoViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val vibrador = rememberVibrador()

    EfectoEventos(viewModel.eventos, snackbarHostState) { evento ->
        if (evento is EventoUI.Exito) vibrador.confirmacion()
    }

    PantallaFormulario(
        titulo = "Nuevo compromiso",
        onVolver = onVolver,
        snackbarHost = { SnackbarPagoFlex(snackbarHostState) }
    ) {
        Text("Asignar compromiso de pago", color = TextoOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            "El estado se asigna según la fecha: Pendiente si aún no vence o Vencido si la fecha ya pasó.",
            color = TextoGris,
            fontSize = 13.sp
        )
        CampoSeleccion(
            valor = state.clienteNombre,
            etiqueta = "Cliente",
            opciones = clientes,
            textoOpcion = { "${it.nombre} (${it.correo})" },
            onSeleccion = viewModel::onClienteChange,
            error = state.clienteError,
            habilitado = !state.cargando
        )
        CampoTexto(
            valor = state.organizacion,
            onValorChange = viewModel::onOrganizacionChange,
            etiqueta = "Organización",
            error = state.organizacionError,
            habilitado = !state.cargando
        )
        CampoTexto(
            valor = state.concepto,
            onValorChange = viewModel::onConceptoChange,
            etiqueta = "Concepto",
            error = state.conceptoError,
            habilitado = !state.cargando
        )
        CampoTexto(
            valor = state.monto,
            onValorChange = viewModel::onMontoChange,
            etiqueta = "Monto ($)",
            error = state.montoError,
            tipoTeclado = KeyboardType.Number,
            habilitado = !state.cargando
        )
        CampoFecha(
            fecha = state.fechaVencimiento,
            etiqueta = "Fecha de vencimiento",
            onFechaSeleccionada = viewModel::onFechaVencimientoChange,
            error = state.fechaError,
            habilitado = !state.cargando
        )
        BotonPrimario(
            texto = if (state.cargando) "Guardando..." else "Asignar compromiso",
            onClick = viewModel::guardarCompromiso,
            habilitado = !state.cargando
        )
    }
}
