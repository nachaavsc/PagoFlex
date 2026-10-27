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

private val FondoClaro = Color(0xFFF8FAFC)
private val AzulCorporativo = Color(0xFF0284C7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeGris = Color(0xFFE2E8F0)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPagosScreen(
    onVolver: () -> Unit
) {
    var periodoSeleccionado by remember { mutableStateOf("Último mes") }
    var empresaSeleccionada by remember { mutableStateOf("Todas") }

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
                        val periodos = listOf("Último mes", "3 meses", "12 meses")
                        items(periodos) { p ->
                            val sel = periodoSeleccionado == p
                            Box(
                                modifier = Modifier
                                    .background(if (sel) AzulCorporativo else TarjetaBlanca, RoundedCornerShape(20.dp))
                                    .border(1.dp, if (sel) AzulCorporativo else BordeGris, RoundedCornerShape(20.dp))
                                    .clickable { periodoSeleccionado = p }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(p, color = if (sel) Color.White else TextoOscuro, fontSize = 13.sp)
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
                        val empresas = listOf("Todas", "Altos del Maipo")
                        items(empresas) { e ->
                            val sel = empresaSeleccionada == e
                            Box(
                                modifier = Modifier
                                    .background(if (sel) AzulCorporativo else TarjetaBlanca, RoundedCornerShape(20.dp))
                                    .border(1.dp, if (sel) AzulCorporativo else BordeGris, RoundedCornerShape(20.dp))
                                    .clickable { empresaSeleccionada = e }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(e, color = if (sel) Color.White else TextoOscuro, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Altos del Maipo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                            Text("CONFIRMADO", fontWeight = FontWeight.Bold, color = AzulCorporativo, fontSize = 12.sp)
                        }
                        Text("Concepto: Crédito de consumo", color = TextoGris, fontSize = 13.sp)
                        Text("Cuota: 2 de 6", color = TextoOscuro, fontSize = 13.sp)
                        Text("Canal de procedencia: App Móvil PagoFlex", color = AzulCorporativo, fontSize = 12.sp)
                        Text("Fecha: 27-10-2026 02:19", color = TextoGris, fontSize = 12.sp)
                        Text("Folio: PF-0002", color = TextoGris, fontSize = 12.sp)
                        Text("Total pagado: \$45.000", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        Text("Comprobante: PF-PAGO-1793078375283", color = TextoGris, fontSize = 11.sp)
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