package com.example.pagoflex

import com.example.pagoflex.ui.util.formatearMonto
import com.example.pagoflex.viewmodel.PeriodoHistorial
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class FormatoYPeriodoTest {

    @Test
    fun formatearMonto_usesChileanThousandsSeparator() {
        assertEquals("$45.000", formatearMonto(45_000L))
        assertEquals("$3.000.000.000", formatearMonto(3_000_000_000L))
    }

    @Test
    fun periodo_desde_subtractsItsDays() {
        val ahora = TimeUnit.DAYS.toMillis(400)
        assertEquals(TimeUnit.DAYS.toMillis(370), PeriodoHistorial.ULTIMO_MES.desde(ahora))
        assertEquals(TimeUnit.DAYS.toMillis(35), PeriodoHistorial.DOCE_MESES.desde(ahora))
    }
}
