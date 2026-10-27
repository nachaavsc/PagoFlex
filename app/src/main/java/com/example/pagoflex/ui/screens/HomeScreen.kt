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
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.viewmodel.HomeViewModel

private val FondoMoradoSuave = Color(0xFFF3E8FF)
private val MoradoPrincipal = Color(0xFF9333EA)
private val MoradoBanner = Color(0xEDA855F7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeLila = Color(0xFFE9D5FF)
private val RosaAlerta = Color(0xFFF43F5E)
private val TextoOscuro = Color(0xFF3B0764)
private val TextoGris = Color(0xFF6B21A8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavegarFormulario: () -> Unit,
    onNavegarPerfil: () -> Unit
) {
    val transacciones by viewModel.transacciones.collectAsState()
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val totalMonto = transacciones.sumOf { it.monto }

    val transaccionesFiltradas = remember(transacciones, filtroSeleccionado) {
        when (filtroSeleccionado) {
            "Completados" -> transacciones.filter { it.estado.contains("Completado", ignoreCase = true) }
            "Pendientes" -> transacciones.filter { it.estado.contains("Pendiente", ignoreCase = true) }
            else -> transacciones
        }
    }

    Scaffold(
        containerColor = FondoMoradoSuave,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tus pagos pendientes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MoradoPrincipal
                ),
                actions = {
                    TextButton(onClick = onNavegarPerfil) {
                        Text("Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavegarFormulario,
                containerColor = MoradoPrincipal,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MoradoBanner)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "\$${String.format("%,.0f", totalMonto).replace(",", ".")}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total de transacciones registradas",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. Alerta de Estado / Compromisos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RosaAlerta)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👤", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tienes ${transacciones.size} compromiso(s) registrado(s).",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
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
                        val opciones = listOf("Todos", "Completados", "Pendientes")
                        items(opciones) { opcion ->
                            val estaSeleccionado = filtroSeleccionado == opcion
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (estaSeleccionado) MoradoPrincipal else TarjetaBlanca,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (estaSeleccionado) MoradoPrincipal else BordeLila,
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
                Text(
                    text = "Cuentas del Mes",
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (transaccionesFiltradas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron compromisos.",
                            color = TextoGris,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(transaccionesFiltradas) { item ->
                    TarjetaCompromiso(item = item)
                }
            }
            item {
                OutlinedButton(
                    onClick = onNavegarPerfil,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MoradoPrincipal))
                ) {
                    Text(
                        text = "Ir a mi perfil PagoFlex",
                        color = MoradoPrincipal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaCompromiso(item: TransaccionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BordeLila, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.concepto,
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "\$${item.monto}",
                    color = MoradoPrincipal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Estado: ${item.estado} • Fecha: ${item.fecha}",
                color = TextoGris,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MoradoPrincipal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Ver detalle para pagar",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}