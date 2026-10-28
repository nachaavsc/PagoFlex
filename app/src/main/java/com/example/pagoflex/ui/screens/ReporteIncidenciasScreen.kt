package com.example.pagoflex.ui.screens

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
import com.example.pagoflex.ui.util.formatearFechaHora
import com.example.pagoflex.viewmodel.IncidenciasViewModel

private val FondoClaro = Color(0xFFF8FAFC)
private val AzulCorporativo = Color(0xFF0284C7)
private val TarjetaBlanca = Color(0xFFFFFFFF)
private val BordeGris = Color(0xFFE2E8F0)
private val TextoOscuro = Color(0xFF0F172A)
private val TextoGris = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteIncidenciasScreen(
    viewModel: IncidenciasViewModel,
    onVolver: () -> Unit
) {
    val incidencias by viewModel.incidencias.collectAsState()
    var motivo by remember { mutableStateOf("Error en el cobro") }
    var detalles by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }

    val motivos = listOf("Error en el cobro", "Pago no reflejado", "Cobro duplicado", "Otro")

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reporte de Incidencias",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Generar constancia de error / reclamo",
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                fontSize = 16.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TarjetaBlanca)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandido,
                        onExpandedChange = { expandido = !expandido }
                    ) {
                        OutlinedTextField(
                            value = motivo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Motivo de la incidencia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulCorporativo,
                                unfocusedBorderColor = BordeGris
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandido,
                            onDismissRequest = { expandido = false }
                        ) {
                            motivos.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        motivo = m
                                        expandido = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = detalles,
                        onValueChange = { detalles = it },
                        label = { Text("Detalle opcional o comentarios") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulCorporativo,
                            unfocusedBorderColor = BordeGris
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.reportarIncidencia(motivo, detalles)
                    detalles = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Enviar reporte de incidencia",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Mis reportes",
                fontWeight = FontWeight.Bold,
                color = TextoOscuro,
                fontSize = 16.sp
            )

            if (incidencias.isEmpty()) {
                Text("Aún no has reportado incidencias.", color = TextoGris, fontSize = 14.sp)
            }

            incidencias.forEach { incidencia ->
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
                            Text(incidencia.motivo, fontWeight = FontWeight.Bold, color = TextoOscuro)
                            Text(incidencia.estado, fontWeight = FontWeight.Bold, color = AzulCorporativo, fontSize = 12.sp)
                        }
                        if (incidencia.detalle.isNotBlank()) {
                            Text(incidencia.detalle, color = TextoGris, fontSize = 13.sp)
                        }
                        Text("Folio: ${incidencia.folio}", color = TextoGris, fontSize = 12.sp)
                        Text("Fecha: ${formatearFechaHora(incidencia.fechaReporte)}", color = TextoGris, fontSize = 12.sp)
                        Text("Constancia: ${incidencia.comprobante}", color = TextoGris, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}