package com.example.pagoflex.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
private val FondoAlertaRojo = Color(0xFFFEF2F2)
private val TextoRojo = Color(0xFF991B1B)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)

data class CuentaDetalle(
    val empresa: String,
    val detalle: String,
    val monto: String,
    val fecha: String,
    val folio: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavegarFormulario: () -> Unit,
    onNavegarPerfil: () -> Unit,
    onNavegarCompromisos: () -> Unit,
    onNavegarHistorial: () -> Unit,
    onNavegarIncidencias: () -> Unit
) {
    var cuentaSeleccionada by remember { mutableStateOf<CuentaDetalle?>(null) }

    Scaffold(
        containerColor = FondoClaro,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCorporativo),
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
                containerColor = AzulCorporativo,
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
                    colors = CardDefaults.cardColors(containerColor = AzulCorporativo)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "$105.000",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Próximo vencimiento: 05-10-2026",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoAlertaRojo)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tienes 1 compromiso de pago vencido.",
                            color = TextoRojo,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavegarCompromisos,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Compromisos", fontSize = 12.sp, color = AzulCorporativo)
                    }
                    OutlinedButton(
                        onClick = onNavegarHistorial,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Historial", fontSize = 12.sp, color = AzulCorporativo)
                    }
                    OutlinedButton(
                        onClick = onNavegarIncidencias,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Incidencias", fontSize = 12.sp, color = AzulCorporativo)
                    }
                }
            }

            item {
                Text(
                    "Cuentas del Mes",
                    color = TextoOscuro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Club Los Halcones", fontWeight = FontWeight.Bold, color = TextoOscuro)
                            Text("$17.000", fontWeight = FontWeight.Bold, color = TextoRojo)
                        }
                        Text("Cuota 10/12 • Vence: 05-10-2026 (Vencida)", color = TextoGris, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                cuentaSeleccionada = CuentaDetalle(
                                    empresa = "Club Los Halcones",
                                    detalle = "Cuota 10/12",
                                    monto = "$17.000",
                                    fecha = "05-10-2026 (Vencida)",
                                    folio = "PF-0010"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                        ) {
                            Text("Ver detalle para pagar", color = Color.White)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plaza Oriente", fontWeight = FontWeight.Bold, color = TextoOscuro)
                            Text("$520.000", fontWeight = FontWeight.Bold, color = AzulCorporativo)
                        }
                        Text("Arriendo octubre • Vence: 10-10-2026", color = TextoGris, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                cuentaSeleccionada = CuentaDetalle(
                                    empresa = "Plaza Oriente",
                                    detalle = "Arriendo octubre",
                                    monto = "$520.000",
                                    fecha = "10-10-2026",
                                    folio = "PF-0011"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                        ) {
                            Text("Ver detalle para pagar", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    cuentaSeleccionada?.let { cuenta ->
        AlertDialog(
            onDismissRequest = { cuentaSeleccionada = null },
            title = {
                Text(
                    text = "Detalle de Pago",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextoOscuro
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Empresa: ${cuenta.empresa}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro)
                    Text(text = "Concepto: ${cuenta.detalle}", fontSize = 14.sp, color = TextoGris)
                    Text(text = "Folio de Cobro: ${cuenta.folio}", fontSize = 14.sp, color = TextoGris)
                    Text(text = "Fecha Vencimiento: ${cuenta.fecha}", fontSize = 14.sp, color = TextoGris)
                    Divider(color = BordeGris)
                    Text(
                        text = "Monto Total: ${cuenta.monto}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AzulCorporativo
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cuentaSeleccionada = null
                        onNavegarFormulario()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo)
                ) {
                    Text("Pagar Ahora", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { cuentaSeleccionada = null }) {
                    Text("Cerrar", color = TextoGris)
                }
            }
        )
    }
}