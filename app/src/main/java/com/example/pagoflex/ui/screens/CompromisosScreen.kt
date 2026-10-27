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
import com.example.pagoflex.viewmodel.HomeViewModel

private val FondoClaro = Color(0xFFF8FAFC)
private val AzulCorporativo = Color(0xFF0284C7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeGris = Color(0xFFE2E8F0)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompromisosScreen(
    viewModel: HomeViewModel,
    onVolver: () -> Unit
) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

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

                    val opciones = listOf("Todos (3)", "Pagados (2)", "Vencidos (0)", "Pendientes (1)")

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(opciones) { opcion ->
                            val estaSeleccionado = filtroSeleccionado in opcion
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
                                    .clickable { filtroSeleccionado = opcion }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = opcion,
                                    color = if (estaSeleccionado) Color.White else TextoOscuro,
                                    fontSize = 14.sp,
                                    fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Normal
                                )
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
                        Text("Altos del Maipo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                        Text("Crédito de consumo", color = TextoGris, fontSize = 13.sp)
                        Text("Cuota: 1 de 6", color = TextoOscuro, fontSize = 14.sp)
                        Text("Monto: \$45.000", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        Text("Vencimiento: 01-10-2026", color = TextoGris, fontSize = 13.sp)
                        Text("Folio: PF-0001", color = TextoGris, fontSize = 13.sp)
                        Text("Estado: Pagado", color = AzulCorporativo, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                        ) {
                            Text("Pagado", color = Color.White)
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