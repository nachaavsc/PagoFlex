package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.model.Transaccion
import com.example.pagoflex.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavegarFormulario: () -> Unit,
    onNavegarPerfil: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val transacciones by viewModel.transacciones.collectAsState(initial = emptyList())

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
        if (transacciones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay transacciones registradas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transacciones) { t: Transaccion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t.concepto, style = MaterialTheme.typography.titleMedium)
                            Text("Monto: $${t.monto}", style = MaterialTheme.typography.bodyMedium)
                            Text("Estado: ${t.estado}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}