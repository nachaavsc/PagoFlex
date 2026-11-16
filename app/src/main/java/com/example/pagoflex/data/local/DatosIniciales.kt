package com.example.pagoflex.data.local

import com.example.pagoflex.data.seguridad.Hash
import java.util.Calendar

object DatosIniciales {

    private const val CANAL_APP = "App Móvil PagoFlex"

    fun usuarios(): List<UsuarioEntity> = listOf(
        UsuarioEntity(
            nombre = "Camila Rojas",
            correo = "cliente@pagoflex.cl",
            clave = Hash.sha256("Cliente#2026"),
            rol = RolUsuario.CLIENTE,
            telefono = "+56912345678",
            direccion = "Av. Providencia 1234, Santiago"
        ),
        UsuarioEntity(
            nombre = "Matías Fuentes",
            correo = "gestor@pagoflex.cl",
            clave = Hash.sha256("Gestor#2026"),
            rol = RolUsuario.GESTOR,
            telefono = "+56987654321",
            direccion = "Huérfanos 950, Santiago"
        )
    )

    fun compromisos(clienteId: Int): List<CompromisoEntity> = listOf(
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0001",
            organizacion = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 1,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(diasDesdeHoy = -60),
            estado = EstadoPago.PAGADO
        ),
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0002",
            organizacion = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 2,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(diasDesdeHoy = -30),
            estado = EstadoPago.PAGADO
        ),
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0003",
            organizacion = "Altos del Maipo",
            concepto = "Crédito de consumo",
            cuotaActual = 3,
            totalCuotas = 6,
            monto = 45_000L,
            fechaVencimiento = fecha(diasDesdeHoy = 15),
            estado = EstadoPago.PENDIENTE
        ),
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0009",
            organizacion = "Club Los Halcones",
            concepto = "Cuota social",
            cuotaActual = 9,
            totalCuotas = 12,
            monto = 17_000L,
            fechaVencimiento = fecha(diasDesdeHoy = -70),
            estado = EstadoPago.PAGADO
        ),
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0010",
            organizacion = "Club Los Halcones",
            concepto = "Cuota social",
            cuotaActual = 10,
            totalCuotas = 12,
            monto = 17_000L,
            fechaVencimiento = fecha(diasDesdeHoy = -10),
            estado = EstadoPago.VENCIDO
        ),
        CompromisoEntity(
            usuarioId = clienteId,
            folio = "PF-0011",
            organizacion = "Plaza Oriente",
            concepto = "Arriendo mensual",
            cuotaActual = 11,
            totalCuotas = 12,
            monto = 520_000L,
            fechaVencimiento = fecha(diasDesdeHoy = 5),
            estado = EstadoPago.PENDIENTE
        )
    )

    fun pagos(compromisos: List<CompromisoEntity>): List<PagoEntity> {
        val porFolio = compromisos.associateBy { it.folio }
        return listOf(
            porFolio.getValue("PF-0009").generarPago(fecha(-71, 18, 40), "Transferencia bancaria"),
            porFolio.getValue("PF-0001").generarPago(fecha(-60, 10, 15), CANAL_APP),
            porFolio.getValue("PF-0002").generarPago(fecha(-31, 14, 19), CANAL_APP)
        )
    }

    fun incidencias(clienteId: Int): List<IncidenciaEntity> {
        val fechaReporte = fecha(-20, 9, 30)
        return listOf(
            IncidenciaEntity(
                usuarioId = clienteId,
                folio = "PF-INC-0001",
                motivo = "Cobro duplicado",
                detalle = "La cuota 2 de 6 (PF-0002) aparece cobrada dos veces en la cartola.",
                fechaReporte = fechaReporte,
                estado = EstadoIncidencia.EN_REVISION,
                comprobante = "PF-INC-$fechaReporte"
            )
        )
    }

    private fun fecha(diasDesdeHoy: Int, hora: Int = 0, minuto: Int = 0): Long =
        Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, diasDesdeHoy)
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
