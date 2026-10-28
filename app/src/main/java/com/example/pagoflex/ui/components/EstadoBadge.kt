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

private data class ColoresEstado(val fondo: Color, val texto: Color)

private fun coloresPara(estado: String): ColoresEstado = when (estado) {
    EstadoPago.PAGADO, EstadoIncidencia.RESUELTA -> ColoresEstado(Color(0xFFDCFCE7), Color(0xFF166534))
    EstadoPago.VENCIDO -> ColoresEstado(Color(0xFFFEE2E2), Color(0xFF991B1B))
    EstadoPago.PENDIENTE -> ColoresEstado(Color(0xFFFEF3C7), Color(0xFF92400E))
    EstadoIncidencia.EN_REVISION -> ColoresEstado(Color(0xFFE0F2FE), Color(0xFF075985))
    else -> ColoresEstado(Color(0xFFF1F5F9), Color(0xFF334155))
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
