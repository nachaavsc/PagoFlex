package com.example.pagoflex.ui.screens

import androidx.compose.foundation.border
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
import com.example.pagoflex.ui.components.EstadoBadge
import com.example.pagoflex.ui.theme.*
import com.example.pagoflex.ui.util.formatearFecha
import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.ui.util.rememberVibrador
import com.example.pagoflex.viewmodel.CompromisosViewModel

private data class OpcionFiltro(val etiqueta: String, val estado: String?)

private val opcionesFiltro = listOf(
    OpcionFiltro("Todos", null),
    OpcionFiltro("Pendientes", EstadoPago.PENDIENTE),
    OpcionFiltro("Pagados", EstadoPago.PAGADO),
    OpcionFiltro("Vencidos", EstadoPago.VENCIDO)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompromisosScreen(
    viewModel: CompromisosViewModel,
    onVolver: () -> Unit,
    onPagar: (Int) -> Unit,
    onNuevoCompromiso: () -> Unit
) {
    val compromisos by viewModel.compromisos.collectAsState()
    val conteo by viewModel.conteoPorEstado.collectAsState()
    val filtroEstado by viewModel.filtroEstado.collectAsState()
    val esGestor by viewModel.esGestor.collectAsState()
    val vibrador = rememberVibrador()

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (esGestor) "Compromisos de clientes" else "Mis compromisos",
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
        },
        floatingActionButton = {
            if (esGestor) {
                ExtendedFloatingActionButton(
                    onClick = onNuevoCompromiso,
                    containerColor = AzulCorporativo,
                    contentColor = Color.White
                ) {
                    Text("Nuevo compromiso", fontWeight = FontWeight.SemiBold)
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
            item {
                Text(
                    text = if (esGestor) "Revisa el estado de todos los compromisos asignados."
                    else "Consulta tus cuotas y paga las pendientes o vencidas.",
                    color = TextoGris,
                    fontSize = 14.sp
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(opcionesFiltro) { opcion ->
                        val cantidad = opcion.estado?.let { conteo[it] ?: 0 } ?: conteo.values.sum()
                        FilterChip(
                            selected = filtroEstado == opcion.estado,
                            onClick = {
                                vibrador.toque()
                                viewModel.onFiltroChange(opcion.estado)
                            },
                            label = { Text("${opcion.etiqueta} ($cantidad)") },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = TarjetaBlanca,
                                labelColor = TextoOscuro,
                                selectedContainerColor = AzulCorporativo,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (compromisos.isEmpty()) {
                item(key = "vacio") {
                    Text(
                        "No hay compromisos para este filtro.",
                        color = TextoGris,
                        fontSize = 14.sp,
                        modifier = Modifier.animateItem()
                    )
                }
            }

            items(compromisos, key = { it.compromiso.id }) { item ->
                val compromiso = item.compromiso
                val pagable = compromiso.estado != EstadoPago.PAGADO
                ElevatedCard(
                    modifier = Modifier
                        .animateItem()
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
                            Text(compromiso.organizacion, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                            EstadoBadge(compromiso.estado)
                        }
                        item.nombreCliente?.let {
                            Text("Cliente: $it", color = AzulCorporativo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(compromiso.concepto, color = TextoGris, fontSize = 13.sp)
                        Text("Cuota: ${compromiso.cuotaActual} de ${compromiso.totalCuotas}", color = TextoOscuro, fontSize = 14.sp)
                        Text("Monto: ${formatearMonto(compromiso.monto)}", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        Text("Vencimiento: ${formatearFecha(compromiso.fechaVencimiento)}", color = TextoGris, fontSize = 13.sp)
                        Text("Folio: ${compromiso.folio}", color = TextoGris, fontSize = 13.sp)

                        if (!esGestor && pagable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    vibrador.toque()
                                    onPagar(compromiso.id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (compromiso.estado == EstadoPago.VENCIDO) Coral else AzulCorporativo
                                )
                            ) {
                                Text("Pagar cuota", color = Color.White)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
