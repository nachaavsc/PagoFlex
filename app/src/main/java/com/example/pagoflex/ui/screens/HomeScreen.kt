package com.example.pagoflex.ui.screens

import com.example.pagoflex.ui.theme.VerdeSuave
import com.example.pagoflex.ui.theme.VerdeOscuro
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.Icons
import com.example.pagoflex.ui.components.EstadoVacio
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.ui.components.EstadoBadge
import com.example.pagoflex.ui.components.TarjetaResumen
import com.example.pagoflex.ui.theme.*
import com.example.pagoflex.ui.util.formatearFecha
import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.viewmodel.HomeUiState
import com.example.pagoflex.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavegarFormulario: () -> Unit,
    onNavegarPerfil: () -> Unit,
    onNavegarCompromisos: () -> Unit,
    onNavegarHistorial: () -> Unit,
    onNavegarIncidencias: () -> Unit,
    onNavegarNuevoCompromiso: () -> Unit,
    onPagar: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hola, ${uiState.nombreUsuario.substringBefore(" ")}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            if (uiState.esGestor) "Gestor de cobranza" else "Cliente",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCorporativo),
                actions = {
                    TextButton(onClick = onNavegarPerfil) {
                        Text("Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.esGestor) {
                FloatingActionButton(
                    onClick = onNavegarFormulario,
                    containerColor = AzulCorporativo,
                    contentColor = Color.White
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.esGestor) {
                inicioGestor(
                    uiState = uiState,
                    onNavegarCompromisos = onNavegarCompromisos,
                    onNavegarIncidencias = onNavegarIncidencias,
                    onNavegarNuevoCompromiso = onNavegarNuevoCompromiso
                )
            } else {
                inicioCliente(
                    uiState = uiState,
                    onNavegarCompromisos = onNavegarCompromisos,
                    onNavegarHistorial = onNavegarHistorial,
                    onNavegarIncidencias = onNavegarIncidencias,
                    onPagar = onPagar
                )
            }
        }
    }
}

private fun LazyListScope.inicioCliente(
    uiState: HomeUiState,
    onNavegarCompromisos: () -> Unit,
    onNavegarHistorial: () -> Unit,
    onNavegarIncidencias: () -> Unit,
    onPagar: (Int) -> Unit
) {
    item {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = AzulMarino)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total por pagar", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text(
                    formatearMonto(uiState.totalPorPagar),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    uiState.proximoVencimiento
                        ?.let { "Próximo vencimiento: ${formatearFecha(it)}" }
                        ?: "Sin vencimientos próximos",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }

    if (uiState.cantidadVencidos > 0) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CoralSuave)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val vencidos = uiState.cantidadVencidos
                    Text(
                        if (vencidos == 1) "Tienes 1 compromiso de pago vencido."
                        else "Tienes $vencidos compromisos de pago vencidos.",
                        color = CoralOscuro,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    item { TarjetaCumplimiento(uiState) }

    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccesoDirecto("Compromisos", onNavegarCompromisos, Modifier.weight(1f))
            AccesoDirecto("Historial", onNavegarHistorial, Modifier.weight(1f))
            AccesoDirecto("Incidencias", onNavegarIncidencias, Modifier.weight(1f))
        }
    }

    item {
        Text(
            "Próximos pagos",
            color = TextoOscuro,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }

    if (uiState.cuentasPorPagar.isEmpty()) {
        item {
            EstadoVacio(
                titulo = "¡Estás al día!",
                mensaje = "No tienes cuentas pendientes por pagar.",
                icono = Icons.Filled.CheckCircle,
                colorIcono = VerdeOscuro,
                fondoIcono = VerdeSuave
            )
        }
    }

    items(uiState.cuentasPorPagar, key = { it.id }) { cuenta ->
        val vencida = cuenta.estado == EstadoPago.VENCIDO
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cuenta.organizacion, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    EstadoBadge(cuenta.estado)
                }
                Text(
                    formatearMonto(cuenta.monto),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (vencida) CoralOscuro else AzulCorporativo
                )
                Text(
                    "Cuota ${cuenta.cuotaActual}/${cuenta.totalCuotas} • Vence: ${formatearFecha(cuenta.fechaVencimiento)}",
                    color = TextoGris,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onPagar(cuenta.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                ) {
                    Text("Pagar", color = Color.White)
                }
            }
        }
    }
}

private fun LazyListScope.inicioGestor(
    uiState: HomeUiState,
    onNavegarCompromisos: () -> Unit,
    onNavegarIncidencias: () -> Unit,
    onNavegarNuevoCompromiso: () -> Unit
) {
    item {
        Text("Resumen de cartera", color = TextoOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }

    item {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen("Clientes", uiState.totalClientes.toString(), AzulCorporativo, Modifier.weight(1f))
                TarjetaResumen("Compromisos", uiState.totalCompromisos.toString(), AzulCorporativo, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen("Por cobrar", formatearMonto(uiState.totalPorPagar), Ambar, Modifier.weight(1f))
                TarjetaResumen("Recaudado", formatearMonto(uiState.montoRecaudado), VerdeEsmeralda, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumen("Vencidos", uiState.cantidadVencidos.toString(), Coral, Modifier.weight(1f))
                TarjetaResumen("Incidencias abiertas", uiState.incidenciasAbiertas.toString(), Ambar, Modifier.weight(1f))
            }
        }
    }

    item { TarjetaCumplimiento(uiState) }

    item {
        Button(
            onClick = onNavegarNuevoCompromiso,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
        ) {
            Text("Asignar nuevo compromiso", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }

    item {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccesoDirecto("Ver compromisos", onNavegarCompromisos, Modifier.weight(1f))
            AccesoDirecto("Revisar incidencias", onNavegarIncidencias, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TarjetaCumplimiento(uiState: HomeUiState) {
    var animar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animar = true }
    val progreso by animateFloatAsState(
        targetValue = if (animar) uiState.porcentajeCumplimiento / 100f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "progresoCumplimiento"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (uiState.esGestor) "Cumplimiento de la cartera" else "Tu cumplimiento financiero",
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "${uiState.porcentajeCumplimiento}%",
                    color = VerdeEsmeralda,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = VerdeEsmeralda,
                trackColor = VerdeSuave
            )
            Text(
                "${uiState.pagosRealizados} de ${uiState.totalCompromisos} compromisos pagados",
                color = TextoGris,
                fontSize = 13.sp
            )
            if (!uiState.esGestor) {
                Text(
                    when (uiState.rachaAlDia) {
                        0 -> "Paga tu próxima cuota a tiempo para iniciar una racha"
                        1 -> "Racha: 1 pago al día"
                        else -> "Racha: ${uiState.rachaAlDia} pagos al día seguidos"
                    },
                    color = if (uiState.rachaAlDia > 0) VerdeOscuro else AmbarOscuro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AccesoDirecto(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(texto, fontSize = 12.sp, color = AzulCorporativo)
    }
}
