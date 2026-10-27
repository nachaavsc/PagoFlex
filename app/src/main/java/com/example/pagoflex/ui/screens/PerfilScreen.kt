package com.example.pagoflex.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onVolver: () -> Unit
) {
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (fotoBitmap != null) {
                Image(
                    bitmap = fotoBitmap!!.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Sin Foto")
                    }
                }
            }

            Text(
                text = "Usuario PagoFlex",
                style = MaterialTheme.typography.titleLarge
            )

            Button(onClick = { cameraLauncher.launch(null) }) {
                Text("Tomar Foto con Cámara")
            }

            TarjetaReferidosYPuntos(
                codigoReferido = "PAGOFLEX-2026",
                puntos = 320,
                metaPuntos = 500
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Inicio")
            }
        }
    }
}

@Composable
private fun TarjetaReferidosYPuntos(
    codigoReferido: String,
    puntos: Int,
    metaPuntos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Programa de Referidos PagoFlex",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Invita a tus amigos y gana puntos cuando realicen su primer pago.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Tu código: $codigoReferido",
                style = MaterialTheme.typography.labelLarge
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Puntos por pago puntual",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$puntos / $metaPuntos pts",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            LinearProgressIndicator(
                progress = { (puntos.toFloat() / metaPuntos).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
