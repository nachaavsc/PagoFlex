package com.example.pagoflex.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.ui.components.BotonPrimario
import com.example.pagoflex.ui.components.CampoSeleccion
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.EfectoEventos
import com.example.pagoflex.ui.components.EstadoBadge
import com.example.pagoflex.ui.components.SnackbarPagoFlex
import com.example.pagoflex.ui.theme.*
import com.example.pagoflex.ui.util.formatearFechaHora
import com.example.pagoflex.ui.util.rememberVibrador
import com.example.pagoflex.viewmodel.EventoUI
import com.example.pagoflex.viewmodel.IncidenciaConCliente
import com.example.pagoflex.viewmodel.IncidenciasViewModel

private val motivos = listOf("Error en el cobro", "Pago no reflejado", "Cobro duplicado", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteIncidenciasScreen(
    viewModel: IncidenciasViewModel,
    onVolver: () -> Unit
) {
    val incidencias by viewModel.incidencias.collectAsState()
    val esGestor by viewModel.esGestor.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val vibrador = rememberVibrador()
    val snackbarHostState = remember { SnackbarHostState() }

    EfectoEventos(viewModel.eventos, snackbarHostState) { evento ->
        if (evento is EventoUI.Exito) vibrador.confirmacion()
    }

    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = { SnackbarPagoFlex(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esGestor) "Gestión de incidencias" else "Reporte de Incidencias",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulCorporativo
                ),
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("Volver", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!esGestor) {
                Text(
                    text = "Generar constancia de error / reclamo",
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    fontSize = 16.sp
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoSeleccion(
                            valor = state.motivo,
                            etiqueta = "Motivo de la incidencia",
                            opciones = motivos,
                            textoOpcion = { it },
                            onSeleccion = viewModel::onMotivoChange,
                            error = state.motivoError,
                            habilitado = !state.cargando
                        )

                        CampoTexto(
                            valor = state.detalle,
                            onValorChange = viewModel::onDetalleChange,
                            etiqueta = "Descripción del problema",
                            error = state.detalleError,
                            habilitado = !state.cargando,
                            lineasMinimas = 4
                        )

                        BotonPrimario(
                            texto = if (state.cargando) "Enviando..." else "Enviar reporte de incidencia",
                            onClick = viewModel::reportarIncidencia,
                            habilitado = !state.cargando
                        )
                    }
                }
            }

            Text(
                text = if (esGestor) "Incidencias reportadas por clientes" else "Mis reportes",
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                fontSize = 16.sp
            )

            if (incidencias.isEmpty()) {
                Text(
                    if (esGestor) "No hay incidencias reportadas." else "Aún no has reportado incidencias.",
                    color = TextoGris,
                    fontSize = 14.sp
                )
            }

            incidencias.forEach { item ->
                TarjetaIncidencia(
                    item = item,
                    esGestor = esGestor,
                    onResolver = { viewModel.resolverIncidencia(item.incidencia) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaIncidencia(
    item: IncidenciaConCliente,
    esGestor: Boolean,
    onResolver: () -> Unit
) {
    val incidencia = item.incidencia
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(incidencia.motivo, fontWeight = FontWeight.Bold, color = TextoOscuro)
                EstadoBadge(incidencia.estado)
            }
            item.nombreCliente?.let {
                Text("Cliente: $it", color = AzulCorporativo, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            if (incidencia.detalle.isNotBlank()) {
                Text(incidencia.detalle, color = TextoGris, fontSize = 13.sp)
            }
            Text("Folio: ${incidencia.folio}", color = TextoGris, fontSize = 12.sp)
            Text("Fecha: ${formatearFechaHora(incidencia.fechaReporte)}", color = TextoGris, fontSize = 12.sp)
            Text("Constancia: ${incidencia.comprobante}", color = TextoGris, fontSize = 11.sp)

            if (esGestor && incidencia.estado == EstadoIncidencia.EN_REVISION) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onResolver,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeEsmeralda),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Marcar como resuelta", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
