package com.example.pagoflex.data.repository

import androidx.room.withTransaction
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.TransaccionEntity
import kotlinx.coroutines.flow.Flow

class PagoFlexRepository(private val db: AppDatabase) {

    private val transaccionDao = db.transaccionDao()
    private val compromisoDao = db.compromisoDao()
    private val pagoDao = db.pagoDao()
    private val incidenciaDao = db.incidenciaDao()

    val todasLasTransacciones: Flow<List<TransaccionEntity>> = transaccionDao.obtenerTodas()

    val compromisos: Flow<List<CompromisoEntity>> = compromisoDao.observarTodos()

    val compromisosNoPagados: Flow<List<CompromisoEntity>> =
        compromisoDao.observarExcluyendoEstado(EstadoPago.PAGADO)

    val empresasConPagos: Flow<List<String>> = pagoDao.observarEmpresas()

    val incidencias: Flow<List<IncidenciaEntity>> = incidenciaDao.observarTodas()

    fun compromisosPorEstado(estado: String?): Flow<List<CompromisoEntity>> =
        if (estado == null) compromisoDao.observarTodos() else compromisoDao.observarPorEstado(estado)

    fun pagosFiltrados(desde: Long, empresa: String?): Flow<List<PagoEntity>> =
        pagoDao.observarFiltrados(desde, empresa)

    suspend fun agregarTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertarTransaccion(transaccion)
    }

    suspend fun registrarPago(compromiso: CompromisoEntity, canal: String, ahora: Long) {
        db.withTransaction {
            compromisoDao.actualizarEstado(compromiso.id, EstadoPago.PAGADO)
            pagoDao.insertar(
                PagoEntity(
                    folio = compromiso.folio,
                    empresa = compromiso.empresa,
                    concepto = compromiso.concepto,
                    cuotaActual = compromiso.cuotaActual,
                    totalCuotas = compromiso.totalCuotas,
                    monto = compromiso.monto,
                    fechaPago = ahora,
                    canal = canal,
                    comprobante = "PF-PAGO-$ahora"
                )
            )
        }
    }

    suspend fun reportarIncidencia(motivo: String, detalle: String, ahora: Long) {
        db.withTransaction {
            val siguiente = incidenciaDao.contar() + 1
            incidenciaDao.insertar(
                IncidenciaEntity(
                    folio = "PF-INC-%04d".format(siguiente),
                    motivo = motivo,
                    detalle = detalle,
                    fechaReporte = ahora,
                    comprobante = "PF-INC-$ahora"
                )
            )
        }
    }
}
