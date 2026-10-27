package com.example.pagoflex.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavegarFormulario: () -> Unit,
    onNavegarPerfil: () -> Unit
) {
    val transacciones by viewModel.transacciones.collectAsState()
    val recordatoriosActivos by viewModel.recordatoriosActivos.collectAsState()
    val context = LocalContext.current

    val permisoNotificacionesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        viewModel.onRecordatoriosChange(concedido)
    }

    val onRecordatoriosToggle: (Boolean) -> Unit = { activar ->
        val requierePermiso = activar &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED

        if (requierePermiso) {
            permisoNotificacionesLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onRecordatoriosChange(activar)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PagoFlex - Historial") },
                actions = {
                    TextButton(onClick = onNavegarPerfil) {
                        Text("Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavegarFormulario) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recordatorios de vencimiento",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = recordatoriosActivos,
                    onCheckedChange = onRecordatoriosToggle
                )
            }

            if (transacciones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay transacciones registradas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transacciones) { t: TransaccionEntity ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(t.concepto, style = MaterialTheme.typography.titleMedium)
                                Text("Monto: \$${t.monto}", style = MaterialTheme.typography.bodyMedium)
                                Text("Estado: ${t.estado}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
