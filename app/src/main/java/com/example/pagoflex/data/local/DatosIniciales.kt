package com.example.pagoflex.data.local

import java.util.Calendar

/** Seed data loaded the first time the database is created. */
object DatosIniciales {

    fun compromisos(): List<CompromisoEntity> = listOf(
        CompromisoEntity(
            folio = "PF-0001",
            empresa = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 1,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(2026, 10, 1),
            estado = EstadoPago.PAGADO
        ),
        CompromisoEntity(
            folio = "PF-0002",
            empresa = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 2,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(2026, 11, 1),
            estado = EstadoPago.PAGADO
        ),
        CompromisoEntity(
            folio = "PF-0003",
            empresa = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 3,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(2026, 12, 1),
            estado = EstadoPago.PENDIENTE
        ),
        CompromisoEntity(
            folio = "PF-0010",
            empresa = "Club Los Halcones",
            concepto = "Cuota social",
            cuotaActual = 10,
            totalCuotas = 12,
            monto = 17_000L,
            fechaVencimiento = fecha(2026, 10, 5),
            estado = EstadoPago.VENCIDO
        ),
        CompromisoEntity(
            folio = "PF-0011",
            empresa = "Plaza Oriente",
            concepto = "Arriendo noviembre",
            cuotaActual = 11,
            totalCuotas = 12,
            monto = 520_000L,
            fechaVencimiento = fecha(2026, 11, 10),
            estado = EstadoPago.PENDIENTE
        )
    )

    fun pagos(): List<PagoEntity> = listOf(
        PagoEntity(
            folio = "PF-0009",
            empresa = "Club Los Halcones",
            concepto = "Cuota social",
            cuotaActual = 9,
            totalCuotas = 12,
            monto = 17_000L,
            fechaPago = fecha(2026, 9, 4, 18, 40),
            canal = "Transferencia bancaria",
            comprobante = "PF-PAGO-1788561600000"
        ),
        PagoEntity(
            folio = "PF-0001",
            empresa = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 1,
            totalCuotas = 6,
            monto = 45_000L,
            fechaPago = fecha(2026, 10, 1, 10, 15),
            canal = "App Móvil PagoFlex",
            comprobante = "PF-PAGO-1790860500000"
        ),
        PagoEntity(
            folio = "PF-0002",
            empresa = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 2,
            totalCuotas = 6,
            monto = 45_000L,
            fechaPago = fecha(2026, 10, 27, 2, 19),
            canal = "App Móvil PagoFlex",
            comprobante = "PF-PAGO-1793078375283"
        )
    )

    fun incidencias(): List<IncidenciaEntity> = listOf(
        IncidenciaEntity(
            folio = "PF-INC-0001",
            motivo = "Cobro duplicado",
            detalle = "La cuota 2 de 6 (PF-0002) aparece cobrada dos veces en la cartola.",
            fechaReporte = fecha(2026, 10, 27, 9, 30),
            estado = EstadoIncidencia.EN_REVISION,
            comprobante = "PF-INC-1793104200000"
        )
    )

    private fun fecha(anio: Int, mes: Int, dia: Int, hora: Int = 0, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(anio, mes - 1, dia, hora, minuto)
        }.timeInMillis
}
