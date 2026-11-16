package com.example.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagoflex.ui.theme.TarjetaBlanca
import com.example.pagoflex.ui.theme.TextoGris
import com.example.pagoflex.ui.theme.TextoOscuro

@Composable
fun TarjetaResumen(
    titulo: String,
    valor: String,
    colorAcento: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = TarjetaBlanca),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 4.dp)
                    .background(colorAcento, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(titulo, color = TextoGris, fontSize = 12.sp)
            Text(valor, color = TextoOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
