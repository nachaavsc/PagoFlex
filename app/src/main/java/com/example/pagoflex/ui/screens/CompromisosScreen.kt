package com.example.pagoflex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.ui.util.formatearFecha
import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.viewmodel.CompromisosViewModel

private val FondoClaro = Color(0xFFF8FAFC)
private val AzulCorporativo = Color(0xFF0284C7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeGris = Color(0xFFE2E8F0)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)
private val TextoRojo = Color(0xFF991B1B)
private val TextoAmbar = Color(0xFFB45309)

private data class OpcionFiltro(val etiqueta: String, val estado: String?)

private val opcionesFiltro = listOf(
    OpcionFiltro("Todos", null),
    OpcionFiltro("Pagados", EstadoPago.PAGADO),
    OpcionFiltro("Vencidos", EstadoPago.VENCIDO),
    OpcionFiltro("Pendientes", EstadoPago.PENDIENTE)
)

private fun colorEstado(estado: String): Color = when (estado) {
    EstadoPago.PAGADO -> AzulCorporativo
    EstadoPago.VENCIDO -> TextoRojo
    else -> TextoAmbar
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompromisosScreen(
    viewModel: CompromisosViewModel,
    onVolver: () -> Unit
) {
    val compromisos by viewModel.compromisos.collectAsState()
    val conteo by viewModel.conteoPorEstado.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis compromisos",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Consulta tus cuotas y su estado de pago.",
                    color = TextoGris,
                    fontSize = 14.sp
                )
            }

            item {
                Column {
                    Text(
                        text = "Filtrar compromisos",
                        color = TextoOscuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(opcionesFiltro) { opcion ->
                            val estaSeleccionado = filtroEstado == opcion.estado
                            val cantidad = opcion.estado?.let { conteo[it] ?: 0 } ?: conteo.values.sum()
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (estaSeleccionado) AzulCorporativo else TarjetaBlanca,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (estaSeleccionado) AzulCorporativo else BordeGris,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.onFiltroChange(opcion.estado) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${opcion.etiqueta} ($cantidad)",
                                    color = if (estaSeleccionado) Color.White else TextoOscuro,
                                    fontSize = 14.sp,
                                    fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            if (compromisos.isEmpty()) {
                item {
                    Text("No hay compromisos para este filtro.", color = TextoGris, fontSize = 14.sp)
                }
            }

            items(compromisos, key = { it.id }) { compromiso ->
                val pagado = compromiso.estado == EstadoPago.PAGADO
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(compromiso.empresa, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                        Text(compromiso.concepto, color = TextoGris, fontSize = 13.sp)
                        Text("Cuota: ${compromiso.cuotaActual} de ${compromiso.totalCuotas}", color = TextoOscuro, fontSize = 14.sp)
                        Text("Monto: ${formatearMonto(compromiso.monto)}", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        Text("Vencimiento: ${formatearFecha(compromiso.fechaVencimiento)}", color = TextoGris, fontSize = 13.sp)
                        Text("Folio: ${compromiso.folio}", color = TextoGris, fontSize = 13.sp)
                        Text(
                            "Estado: ${compromiso.estado}",
                            color = colorEstado(compromiso.estado),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.registrarPago(compromiso) },
                            enabled = !pagado,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                        ) {
                            Text(if (pagado) "Pagado" else "Registrar pago", color = Color.White)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}