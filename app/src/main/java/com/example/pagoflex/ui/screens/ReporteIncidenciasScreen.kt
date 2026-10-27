package com.example.pagoflex.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
fun ReporteIncidenciasScreen(
    onVolver: () -> Unit
) {
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onVolver,
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
        }
    }
}