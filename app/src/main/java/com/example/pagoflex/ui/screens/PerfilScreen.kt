package com.example.pagoflex.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.ui.theme.*
import com.example.pagoflex.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit,
    onEditarPerfil: () -> Unit,
    onCambiarClave: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val context = LocalContext.current
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil de Usuario",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(AzulSuave)
                    .border(2.dp, AzulCorporativo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (fotoBitmap != null) {
                    Image(
                        bitmap = fotoBitmap!!.asImageBitmap(),
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "Sin Foto",
                        color = AzulCorporativo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = usuario?.nombre ?: "Usuario PagoFlex",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )

            Text(
                text = usuario?.correo.orEmpty(),
                fontSize = 14.sp,
                color = TextoGris
            )

            Text(
                text = if (usuario?.rol == RolUsuario.GESTOR) "Gestor de cobranza" else "Cliente",
                color = AzulCorporativo,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .background(AzulSuave, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Button(
                onClick = {
                    try {
                        launcherCamara.launch(null)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir la cámara en este dispositivo", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tomar Foto con Cámara", color = Color.White, fontWeight = FontWeight.Bold)
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BordeGris, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Programa de Referidos PagoFlex",
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Invita a tus amigos y gana puntos cuando realicen su primer pago.",
                        color = TextoGris,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Tu código: PAGOFLEX-2026",
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Puntos acumulados:", fontSize = 13.sp, color = TextoOscuro)
                        Text("320 / 500 pts", fontWeight = FontWeight.Bold, color = AzulCorporativo, fontSize = 13.sp)
                    }
                    LinearProgressIndicator(
                        progress = { 0.64f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AzulCorporativo,
                        trackColor = BordeGris
                    )
                }
            }

            OutlinedButton(
                onClick = onEditarPerfil,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Editar perfil", color = AzulCorporativo, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCambiarClave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cambiar clave", color = AzulCorporativo, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Coral),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}