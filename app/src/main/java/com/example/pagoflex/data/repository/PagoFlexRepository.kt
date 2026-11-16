package com.example.pagoflex.data.repository

import androidx.room.withTransaction
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.local.generarPago
import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class PagoFlexRepository(private val db: AppDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val transaccionDao = db.transaccionDao()
    private val compromisoDao = db.compromisoDao()
    private val pagoDao = db.pagoDao()
    private val incidenciaDao = db.incidenciaDao()

    private val _usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioActual: StateFlow<UsuarioEntity?> = _usuarioActual.asStateFlow()

    val todasLasTransacciones: Flow<List<TransaccionEntity>> = transaccionDao.obtenerTodas()

    val compromisos: Flow<List<CompromisoEntity>> = compromisoDao.observarTodos()

    val incidencias: Flow<List<IncidenciaEntity>> = incidenciaDao.observarTodas()

    val clientes: Flow<List<UsuarioEntity>> = usuarioDao.observarPorRol(RolUsuario.CLIENTE)

    fun compromisosDeUsuario(usuarioId: Int): Flow<List<CompromisoEntity>> =
        compromisoDao.observarPorUsuario(usuarioId)

    fun historialDeUsuario(usuarioId: Int): Flow<List<PagoEntity>> =
        pagoDao.observarHistorialPorUsuario(usuarioId)

    fun incidenciasDeUsuario(usuarioId: Int): Flow<List<IncidenciaEntity>> =
        incidenciaDao.observarPorUsuario(usuarioId)

    fun observarUsuario(id: Int): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    suspend fun iniciarSesion(correo: String, clave: String): UsuarioEntity? {
        actualizarVencidos()
        return usuarioDao.autenticar(normalizarCorreo(correo), Hash.sha256(clave))
            .also { _usuarioActual.value = it }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    suspend fun existeCorreo(correo: String, excluirId: Int? = null): Boolean =
        usuarioDao.obtenerPorCorreo(normalizarCorreo(correo))?.let { it.id != excluirId } ?: false

    suspend fun registrarUsuario(nombre: String, correo: String, telefono: String, clave: String): Long =
        usuarioDao.insertar(
            UsuarioEntity(
                nombre = nombre.trim(),
                correo = normalizarCorreo(correo),
                clave = Hash.sha256(clave),
                rol = RolUsuario.CLIENTE,
                telefono = Validador.normalizarTelefono(telefono)
            )
        )

    suspend fun restablecerClave(correo: String, telefono: String, nuevaClave: String): Boolean {
        val usuario = usuarioDao.obtenerPorCorreo(normalizarCorreo(correo)) ?: return false
        if (usuario.telefono != Validador.normalizarTelefono(telefono)) return false
        usuarioDao.actualizarClave(usuario.id, Hash.sha256(nuevaClave))
        return true
    }

    suspend fun actualizarPerfil(nombre: String, correo: String, telefono: String, direccion: String) {
        val actual = _usuarioActual.value ?: return
        val actualizado = actual.copy(
            nombre = nombre.trim(),
            correo = normalizarCorreo(correo),
            telefono = Validador.normalizarTelefono(telefono),
            direccion = direccion.trim()
        )
        usuarioDao.actualizar(actualizado)
        _usuarioActual.value = actualizado
    }

    fun verificarClave(clave: String): Boolean =
        _usuarioActual.value?.clave == Hash.sha256(clave)

    suspend fun cambiarClave(claveActual: String, nuevaClave: String): Boolean {
        val usuario = _usuarioActual.value ?: return false
        if (!verificarClave(claveActual)) return false
        val nuevoHash = Hash.sha256(nuevaClave)
        usuarioDao.actualizarClave(usuario.id, nuevoHash)
        _usuarioActual.value = usuario.copy(clave = nuevoHash)
        return true
    }

    suspend fun obtenerCompromiso(id: Int): CompromisoEntity? = compromisoDao.obtenerPorId(id)

    suspend fun actualizarVencidos() {
        compromisoDao.marcarVencidos(inicioDelDia(System.currentTimeMillis()))
    }

    suspend fun registrarCompromiso(
        clienteId: Int,
        organizacion: String,
        concepto: String,
        monto: Long,
        fechaVencimiento: Long
    ): CompromisoEntity = db.withTransaction {
        val estado = if (fechaVencimiento < inicioDelDia(System.currentTimeMillis())) {
            EstadoPago.VENCIDO
        } else {
            EstadoPago.PENDIENTE
        }
        val compromiso = CompromisoEntity(
            usuarioId = clienteId,
            folio = siguienteFolio(compromisoDao.obtenerFolios()),
            organizacion = organizacion.trim(),
            concepto = concepto.trim(),
            cuotaActual = 1,
            totalCuotas = 1,
            monto = monto,
            fechaVencimiento = fechaVencimiento,
            estado = estado
        )
        compromiso.copy(id = compromisoDao.insertar(compromiso).toInt())
    }

    suspend fun agregarTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertarTransaccion(transaccion)
    }

    suspend fun registrarPago(
        compromiso: CompromisoEntity,
        canal: String,
        ahora: Long,
        rutaComprobanteFoto: String? = null
    ): PagoEntity = db.withTransaction {
        val pago = compromiso.generarPago(ahora, canal, rutaComprobanteFoto)
        compromisoDao.marcarComoPagado(compromiso.id)
        pago.copy(id = pagoDao.insertar(pago).toInt())
    }

    suspend fun reportarIncidencia(motivo: String, detalle: String, ahora: Long): IncidenciaEntity? {
        val usuario = _usuarioActual.value ?: return null
        return db.withTransaction {
            val siguiente = incidenciaDao.contar() + 1
            val incidencia = IncidenciaEntity(
                usuarioId = usuario.id,
                folio = "PF-INC-%04d".format(siguiente),
                motivo = motivo,
                detalle = detalle,
                fechaReporte = ahora,
                comprobante = "PF-INC-$ahora"
            )
            incidencia.copy(id = incidenciaDao.insertar(incidencia).toInt())
        }
    }

    suspend fun resolverIncidencia(id: Int) {
        incidenciaDao.actualizarEstado(id, EstadoIncidencia.RESUELTA)
    }

    private fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    private fun siguienteFolio(folios: List<String>): String {
        val mayor = folios.mapNotNull { it.removePrefix("PF-").toIntOrNull() }.maxOrNull() ?: 0
        return "PF-%04d".format(mayor + 1)
    }

    private fun inicioDelDia(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
