package com.example.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.theme.AzulCorporativo
import com.example.pagoflex.ui.theme.CoralOscuro
import com.example.pagoflex.ui.theme.CoralSuave
import com.example.pagoflex.ui.theme.FondoClaro
import com.example.pagoflex.ui.theme.TarjetaBlanca

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    titulo: String?,
    onVolver: (() -> Unit)?,
    encabezado: @Composable ColumnScope.() -> Unit = {},
    pie: @Composable ColumnScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = snackbarHost,
        topBar = {
            if (titulo != null) {
                TopAppBar(
                    title = {
                        Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCorporativo),
                    navigationIcon = {
                        if (onVolver != null) {
                            TextButton(onClick = onVolver) {
                                Text("Volver", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            encabezado()
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = contenido
                )
            }
            pie()
        }
    }
}

@Composable
fun MensajeError(mensaje: String?, modifier: Modifier = Modifier) {
    if (mensaje == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(CoralSuave, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = CoralOscuro,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(mensaje, color = CoralOscuro, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
