package com.example.pagoflex.model

import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.PagoEntity
import java.util.concurrent.TimeUnit

object Cumplimiento {

    private val UN_DIA_MS = TimeUnit.DAYS.toMillis(1)

    fun porcentaje(compromisos: List<CompromisoEntity>): Int {
        if (compromisos.isEmpty()) return 0
        val pagados = compromisos.count { it.estado == EstadoPago.PAGADO }
        return pagados * 100 / compromisos.size
    }

    fun racha(pagos: List<PagoEntity>, compromisos: List<CompromisoEntity>): Int {
        val vencimientos = compromisos.associate { it.id to it.fechaVencimiento }
        return pagos
            .sortedByDescending { it.fechaPago }
            .takeWhile { pago ->
                vencimientos[pago.compromisoId]?.let { vence -> pago.fechaPago < vence + UN_DIA_MS } ?: false
            }
            .size
    }
}
