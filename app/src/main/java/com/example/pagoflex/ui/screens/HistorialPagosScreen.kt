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
import com.example.pagoflex.ui.components.EstadoBadge
import com.example.pagoflex.ui.util.formatearFechaHora
import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.viewmodel.HistorialViewModel
import com.example.pagoflex.viewmodel.PeriodoHistorial

private val FondoClaro = Color(0xFFF8FAFC)
private val AzulCorporativo = Color(0xFF0284C7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeGris = Color(0xFFE2E8F0)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPagosScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit
) {
    val pagos by viewModel.pagos.collectAsState()
    val empresas by viewModel.empresas.collectAsState()
    val periodoSeleccionado by viewModel.periodo.collectAsState()
    val empresaSeleccionada by viewModel.empresa.collectAsState()

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial de pagos",
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
                    text = "Consulta los pagos realizados desde cualquier canal.",
                    color = TextoGris,
                    fontSize = 14.sp
                )
            }

            item {
                Column {
                    Text("Filtrar por período:", fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PeriodoHistorial.entries) { p ->
                            val sel = periodoSeleccionado == p
                            Box(
                                modifier = Modifier
                                    .background(if (sel) AzulCorporativo else TarjetaBlanca, RoundedCornerShape(20.dp))
                                    .border(1.dp, if (sel) AzulCorporativo else BordeGris, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.onPeriodoChange(p) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(p.etiqueta, color = if (sel) Color.White else TextoOscuro, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Text("Filtrar por empresa:", fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf<String?>(null) + empresas) { e ->
                            val sel = empresaSeleccionada == e
                            Box(
                                modifier = Modifier
                                    .background(if (sel) AzulCorporativo else TarjetaBlanca, RoundedCornerShape(20.dp))
                                    .border(1.dp, if (sel) AzulCorporativo else BordeGris, RoundedCornerShape(20.dp))
                                    .clickable { viewModel.onEmpresaChange(e) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(e ?: "Todas", color = if (sel) Color.White else TextoOscuro, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            if (pagos.isEmpty()) {
                item {
                    Text("No hay pagos registrados en este período.", color = TextoGris, fontSize = 14.sp)
                }
            }

            items(pagos, key = { it.id }) { pago ->
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pago.empresa, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                            EstadoBadge(pago.estado)
                        }
                        Text("Concepto: ${pago.concepto}", color = TextoGris, fontSize = 13.sp)
                        Text("Cuota: ${pago.cuotaActual} de ${pago.totalCuotas}", color = TextoOscuro, fontSize = 13.sp)
                        Text("Canal de procedencia: ${pago.canal}", color = AzulCorporativo, fontSize = 12.sp)
                        Text("Fecha: ${formatearFechaHora(pago.fechaPago)}", color = TextoGris, fontSize = 12.sp)
                        Text("Folio: ${pago.folio}", color = TextoGris, fontSize = 12.sp)
                        Text("Total pagado: ${formatearMonto(pago.monto)}", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        Text("Comprobante: ${pago.comprobante}", color = TextoGris, fontSize = 11.sp)
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