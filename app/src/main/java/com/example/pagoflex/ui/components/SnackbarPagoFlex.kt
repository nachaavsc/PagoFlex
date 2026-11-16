package com.example.pagoflex.ui.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.example.pagoflex.ui.theme.CoralOscuro
import com.example.pagoflex.ui.theme.VerdeOscuro
import com.example.pagoflex.viewmodel.EventoUI
import kotlinx.coroutines.flow.Flow

private class VisualesEvento(
    override val message: String,
    val esError: Boolean
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

@Composable
fun SnackbarPagoFlex(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        val esError = (data.visuals as? VisualesEvento)?.esError == true
        Snackbar(
            snackbarData = data,
            containerColor = if (esError) CoralOscuro else VerdeOscuro,
            contentColor = Color.White
        )
    }
}

@Composable
fun EfectoEventos(
    eventos: Flow<EventoUI>,
    hostState: SnackbarHostState,
    onEvento: (EventoUI) -> Unit = {}
) {
    LaunchedEffect(eventos) {
        eventos.collect { evento ->
            onEvento(evento)
            hostState.showSnackbar(VisualesEvento(evento.mensaje, evento is EventoUI.Error))
        }
    }
}
