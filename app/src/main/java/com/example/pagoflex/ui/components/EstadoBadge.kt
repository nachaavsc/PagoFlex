package com.example.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.pagoflex.ui.theme.TextoGris
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

@Composable
fun EstadoBadge(estado: String, modifier: Modifier = Modifier) {
    val colores = coloresPara(estado)
    Text(
        text = estado,
        color = colores.texto,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(colores.fondo, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
