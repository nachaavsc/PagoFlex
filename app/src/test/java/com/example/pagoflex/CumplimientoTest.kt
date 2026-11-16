package com.example.pagoflex

import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.generarPago
import com.example.pagoflex.model.Cumplimiento
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class CumplimientoTest {

    private val dia = TimeUnit.DAYS.toMillis(1)

    private fun compromiso(id: Int, vencimiento: Long, estado: String) = CompromisoEntity(
        id = id,
        usuarioId = 1,
        folio = "PF-%04d".format(id),
        organizacion = "Plaza Oriente",
        concepto = "Arriendo",
        cuotaActual = id,
        totalCuotas = 12,
        monto = 10_000L,
        fechaVencimiento = vencimiento,
        estado = estado
    )

    @Test
    fun porcentaje_calculaPagadosSobreTotal() {
        val compromisos = listOf(
            compromiso(1, 0, EstadoPago.PAGADO),
            compromiso(2, 0, EstadoPago.PAGADO),
            compromiso(3, 0, EstadoPago.PENDIENTE),
            compromiso(4, 0, EstadoPago.VENCIDO)
        )
        assertEquals(50, Cumplimiento.porcentaje(compromisos))
        assertEquals(0, Cumplimiento.porcentaje(emptyList()))
    }

    @Test
    fun racha_cuentaPagosAlDiaConsecutivosDesdeElMasReciente() {
        val c1 = compromiso(1, 10 * dia, EstadoPago.PAGADO)
        val c2 = compromiso(2, 40 * dia, EstadoPago.PAGADO)
        val c3 = compromiso(3, 70 * dia, EstadoPago.PAGADO)
        val c4 = compromiso(4, 100 * dia, EstadoPago.PAGADO)
        val pagos = listOf(
            c1.generarPago(9 * dia, "App"),
            c2.generarPago(45 * dia, "App"),
            c3.generarPago(70 * dia + dia / 2, "App"),
            c4.generarPago(99 * dia, "App")
        )

        assertEquals(2, Cumplimiento.racha(pagos, listOf(c1, c2, c3, c4)))
        assertEquals(0, Cumplimiento.racha(emptyList(), listOf(c1)))
    }
}
