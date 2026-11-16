package com.example.pagoflex.data.repository

import androidx.room.withTransaction
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.local.generarPago
import kotlinx.coroutines.flow.Flow

class PagoFlexRepository(private val db: AppDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val transaccionDao = db.transaccionDao()
    private val compromisoDao = db.compromisoDao()
    private val pagoDao = db.pagoDao()
    private val incidenciaDao = db.incidenciaDao()

    val todasLasTransacciones: Flow<List<TransaccionEntity>> = transaccionDao.obtenerTodas()

    val compromisos: Flow<List<CompromisoEntity>> = compromisoDao.observarTodos()

    val compromisosNoPagados: Flow<List<CompromisoEntity>> = compromisoDao.observarNoPagados()

    val organizacionesConPagos: Flow<List<String>> = pagoDao.observarOrganizaciones()

    val incidencias: Flow<List<IncidenciaEntity>> = incidenciaDao.observarTodas()

    val clientes: Flow<List<UsuarioEntity>> = usuarioDao.observarPorRol(RolUsuario.CLIENTE)

    fun compromisosPorEstado(estado: String?): Flow<List<CompromisoEntity>> =
        if (estado == null) compromisoDao.observarTodos() else compromisoDao.observarPorEstado(estado)

    fun compromisosDeUsuario(usuarioId: Int): Flow<List<CompromisoEntity>> =
        compromisoDao.observarPorUsuario(usuarioId)

    fun compromisosNoPagadosDeUsuario(usuarioId: Int): Flow<List<CompromisoEntity>> =
        compromisoDao.observarNoPagadosPorUsuario(usuarioId)

    fun historialDeUsuario(usuarioId: Int): Flow<List<PagoEntity>> =
        pagoDao.observarHistorialPorUsuario(usuarioId)

    fun pagosFiltrados(desde: Long, organizacion: String?): Flow<List<PagoEntity>> =
        pagoDao.observarFiltrados(desde, organizacion)

    fun observarUsuario(id: Int): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    suspend fun autenticar(correo: String, clave: String): UsuarioEntity? =
        usuarioDao.autenticar(correo.trim().lowercase(), clave)

    suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? =
        usuarioDao.obtenerPorCorreo(correo.trim().lowercase())

    suspend fun registrarUsuario(usuario: UsuarioEntity): Long =
        usuarioDao.insertar(usuario.copy(correo = usuario.correo.trim().lowercase()))

    suspend fun actualizarUsuario(usuario: UsuarioEntity) {
        usuarioDao.actualizar(usuario.copy(correo = usuario.correo.trim().lowercase()))
    }

    suspend fun actualizarClave(usuarioId: Int, nuevaClave: String) {
        usuarioDao.actualizarClave(usuarioId, nuevaClave)
    }

    suspend fun registrarCompromiso(compromiso: CompromisoEntity): Long =
        compromisoDao.insertar(compromiso)

    suspend fun agregarTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertarTransaccion(transaccion)
    }

    suspend fun registrarPago(
        compromiso: CompromisoEntity,
        canal: String,
        ahora: Long,
        rutaComprobanteFoto: String? = null
    ) {
        db.withTransaction {
            compromisoDao.marcarComoPagado(compromiso.id)
            pagoDao.insertar(compromiso.generarPago(ahora, canal, rutaComprobanteFoto))
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
