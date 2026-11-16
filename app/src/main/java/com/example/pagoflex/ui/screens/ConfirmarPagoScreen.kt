package com.example.pagoflex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.EfectoEventos
import com.example.pagoflex.ui.components.EstadoBadge
import com.example.pagoflex.ui.components.PantallaFormulario
import com.example.pagoflex.ui.components.SnackbarPagoFlex
import com.example.pagoflex.ui.theme.AzulCorporativo
import com.example.pagoflex.ui.theme.BordeGris
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.theme.TextoOscuro
import com.example.pagoflex.ui.theme.VerdeOscuro
import com.example.pagoflex.ui.theme.VerdeSuave
import com.example.pagoflex.ui.util.formatearFecha
import com.example.pagoflex.ui.util.formatearFechaHora
import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.viewmodel.ConfirmarPagoViewModel

@Composable
fun ConfirmarPagoScreen(
    viewModel: ConfirmarPagoViewModel,
    compromisoId: Int,
    onVolver: () -> Unit,
    onVerHistorial: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(compromisoId) { viewModel.cargar(compromisoId) }
    EfectoEventos(viewModel.eventos, snackbarHostState)

    PantallaFormulario(
        titulo = "Confirmar pago",
        onVolver = onVolver,
        snackbarHost = { SnackbarPagoFlex(snackbarHostState) }
    ) {
        val compromiso = state.compromiso
        if (compromiso == null) {
            Text("Cargando compromiso...", color = TextoGris)
        } else {
            DetallePago(
                compromiso = compromiso,
                pago = state.pagoRegistrado,
                cargando = state.cargando,
                onConfirmar = viewModel::confirmarPago,
                onVolver = onVolver,
                onVerHistorial = onVerHistorial
            )
        }
    }
}

@Composable
private fun DetallePago(
    compromiso: CompromisoEntity,
    pago: PagoEntity?,
    cargando: Boolean,
    onConfirmar: () -> Unit,
    onVolver: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(compromiso.organizacion, color = TextoOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            EstadoBadge(compromiso.estado)
        }
        Text(compromiso.concepto, color = TextoGris, fontSize = 14.sp)
        Text("Cuota ${compromiso.cuotaActual} de ${compromiso.totalCuotas}", color = TextoOscuro, fontSize = 14.sp)
        Text("Folio: ${compromiso.folio}", color = TextoGris, fontSize = 14.sp)
        Text("Vencimiento: ${formatearFecha(compromiso.fechaVencimiento)}", color = TextoGris, fontSize = 14.sp)
        HorizontalDivider(color = BordeGris, modifier = Modifier.padding(vertical = 8.dp))
        Text("Monto a pagar", color = TextoGris, fontSize = 13.sp)
        Text(formatearMonto(compromiso.monto), color = AzulCorporativo, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)

        if (pago == null) {
            Text(
                "Al confirmar, el compromiso quedará marcado como pagado con la fecha de hoy.",
                color = TextoGris,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            BotonPrimario(
                texto = if (cargando) "Procesando..." else "Confirmar pago",
                onClick = onConfirmar,
                habilitado = !cargando
            )
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = AzulCorporativo)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerdeSuave, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Pago registrado", color = VerdeOscuro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Comprobante: ${pago.comprobante}", color = VerdeOscuro, fontSize = 13.sp)
                Text("Fecha: ${formatearFechaHora(pago.fechaPago)}", color = VerdeOscuro, fontSize = 13.sp)
            }
            BotonPrimario(texto = "Ver historial de pagos", onClick = onVerHistorial)
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Volver", color = AzulCorporativo)
            }
        }
    }
}
