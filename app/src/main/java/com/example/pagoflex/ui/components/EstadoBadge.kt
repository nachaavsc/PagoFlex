package com.example.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.ui.theme.AmbarOscuro
import com.example.pagoflex.ui.theme.AmbarSuave
import com.example.pagoflex.ui.theme.AzulCorporativo
import com.example.pagoflex.ui.theme.AzulSuave
import com.example.pagoflex.ui.theme.BordeGris
import com.example.pagoflex.ui.theme.CoralOscuro
import com.example.pagoflex.ui.theme.CoralSuave
import com.example.pagoflex.ui.theme.TarjetaBlanca
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.theme.TextoOscuro
import com.example.pagoflex.ui.theme.VerdeOscuro
import com.example.pagoflex.ui.theme.VerdeSuave

private data class ColoresEstado(val fondo: Color, val texto: Color)

private fun coloresPara(estado: String): ColoresEstado = when (estado) {
    EstadoPago.PAGADO, EstadoIncidencia.RESUELTA -> ColoresEstado(VerdeSuave, VerdeOscuro)
    EstadoPago.VENCIDO -> ColoresEstado(CoralSuave, CoralOscuro)
    EstadoPago.PENDIENTE -> ColoresEstado(AmbarSuave, AmbarOscuro)
    EstadoIncidencia.EN_REVISION -> ColoresEstado(AzulSuave, AzulCorporativo)
    else -> ColoresEstado(BordeGris, TextoGris)
}

private fun etiquetaPara(estado: String): String =
    estado.lowercase().replaceFirstChar { it.uppercase() }

@Composable
fun EstadoBadge(estado: String, modifier: Modifier = Modifier) {
    val colores = coloresPara(estado)
    AssistChip(
        onClick = {},
        label = { Text(etiquetaPara(estado), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        shape = RoundedCornerShape(12.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colores.fondo,
            labelColor = colores.texto
        ),
        border = null,
        modifier = modifier
    )
}

@Composable
fun EstadoVacio(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    icono: ImageVector = Icons.Filled.Info,
    colorIcono: Color = AzulCorporativo,
    fondoIcono: Color = AzulSuave
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TarjetaBlanca, RoundedCornerShape(16.dp))
            .border(1.dp, BordeGris, RoundedCornerShape(16.dp))
            .padding(vertical = 28.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(shape = RoundedCornerShape(50), color = fondoIcono) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier
                    .padding(12.dp)
                    .size(28.dp)
            )
        }
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoOscuro, textAlign = TextAlign.Center)
        Text(mensaje, fontSize = 14.sp, color = TextoGris, textAlign = TextAlign.Center)
    }
}
