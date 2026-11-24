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
import com.example.pagoflex.data.remote.CompromisoRemoto
import com.example.pagoflex.data.remote.IncidenciaRemota
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.remote.PagoRemoto
import com.example.pagoflex.data.remote.RetrofitClient
import com.example.pagoflex.data.remote.UsuarioRemoto
import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

class PagoFlexRepository(
    private val db: AppDatabase,
    private val api: PagoFlexApi = RetrofitClient.api
) {

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
        val correoNormalizado = normalizarCorreo(correo)
        val claveHash = Hash.sha256(clave)
        val usuario = try {
            val respuesta = api.login(mapOf("correo" to correoNormalizado, "clave" to claveHash))
            val credencial = respuesta.body()
            if (!respuesta.isSuccessful || credencial == null) {
                null
            } else {
                guardarUsuarioRemoto(llamar { api.obtenerUsuario(credencial.usuarioId) }, claveHash)
            }
        } catch (e: IOException) {
            usuarioDao.autenticar(correoNormalizado, claveHash)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalStateException) {
            null
        }
        _usuarioActual.value = usuario
        if (usuario != null) sincronizar()
        return usuario
    }

    suspend fun sincronizar(): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return try {
            val esGestor = usuario.rol == RolUsuario.GESTOR
            if (esGestor) llamar { api.listarUsuarios() }.forEach { guardarUsuarioRemoto(it) }
            val compromisosRemotos = if (esGestor) {
                llamar { api.listarCompromisos() }
            } else {
                llamar { api.listarCompromisosPorUsuario(usuario.id.toLong()) }
            }
            compromisosRemotos.forEach { remoto ->
                val compromiso = guardarCompromisoRemoto(remoto) ?: return@forEach
                llamar { api.listarPagos(compromiso.id.toLong()) }.forEach { guardarPagoRemoto(it, compromiso) }
            }
            val incidenciasRemotas = if (esGestor) {
                llamar { api.listarIncidencias() }
            } else {
                llamar { api.listarIncidenciasPorUsuario(usuario.id.toLong()) }
            }
            incidenciasRemotas.forEach { guardarIncidenciaRemota(it) }
            actualizarVencidos()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }

    suspend fun existeCorreo(correo: String, excluirId: Int? = null): Boolean =
        usuarioDao.obtenerPorCorreo(normalizarCorreo(correo))?.let { it.id != excluirId } ?: false

    suspend fun registrarUsuario(nombre: String, correo: String, telefono: String, clave: String): Long =
        llamar {
            api.registro(
                mapOf(
                    "nombre" to nombre.trim(),
                    "correo" to normalizarCorreo(correo),
                    "clave" to Hash.sha256(clave),
                    "telefono" to Validador.normalizarTelefono(telefono)
                )
            )
        }.usuarioId

    suspend fun restablecerClave(correo: String, telefono: String, nuevaClave: String): Boolean {
        val correoNormalizado = normalizarCorreo(correo)
        val respuesta = api.obtenerUsuarioPorCorreo(correoNormalizado)
        val remoto = respuesta.body()
        if (!respuesta.isSuccessful || remoto == null) return false
        if (Validador.normalizarTelefono(remoto.telefono.orEmpty()) != Validador.normalizarTelefono(telefono)) return false
        val nuevoHash = Hash.sha256(nuevaClave)
        llamar { api.cambiarClave(mapOf("correo" to correoNormalizado, "nuevaClave" to nuevoHash)) }
        usuarioDao.obtenerPorCorreo(correoNormalizado)?.let { usuarioDao.actualizarClave(it.id, nuevoHash) }
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
        llamar {
            api.actualizarUsuario(
                actual.id.toLong(),
                UsuarioRemoto(nombre = actualizado.nombre, telefono = actualizado.telefono)
            )
        }
        usuarioDao.actualizar(actualizado)
        _usuarioActual.value = actualizado
    }

    fun verificarClave(clave: String): Boolean =
        _usuarioActual.value?.clave == Hash.sha256(clave)

    suspend fun cambiarClave(claveActual: String, nuevaClave: String): Boolean {
        val usuario = _usuarioActual.value ?: return false
        if (!verificarClave(claveActual)) return false
        val nuevoHash = Hash.sha256(nuevaClave)
        llamar { api.cambiarClave(mapOf("correo" to usuario.correo, "nuevaClave" to nuevoHash)) }
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
    ): CompromisoEntity {
        val estado = if (fechaVencimiento < inicioDelDia(System.currentTimeMillis())) {
            EstadoPago.VENCIDO
        } else {
            EstadoPago.PENDIENTE
        }
        val remoto = llamar {
            api.crearCompromiso(
                CompromisoRemoto(
                    usuarioId = clienteId.toLong(),
                    titulo = organizacion.trim(),
                    descripcion = concepto.trim(),
                    montoTotal = monto.toDouble(),
                    cuotasTotales = 1,
                    fechaVencimiento = formatearFecha(fechaVencimiento),
                    estado = estado
                )
            )
        }
        return guardarCompromisoRemoto(remoto) ?: throw IllegalStateException("No se pudo guardar el compromiso")
    }

    suspend fun agregarTransaccion(transaccion: TransaccionEntity) {
        transaccionDao.insertarTransaccion(transaccion)
    }

    suspend fun registrarPago(
        compromiso: CompromisoEntity,
        canal: String,
        ahora: Long,
        rutaComprobanteFoto: String? = null
    ): PagoEntity {
        val pago = compromiso.generarPago(ahora, canal, rutaComprobanteFoto)
        val remoto = llamar {
            api.registrarPago(
                compromiso.id.toLong(),
                PagoRemoto(
                    compromisoId = compromiso.id.toLong(),
                    monto = pago.montoPagado.toDouble(),
                    metodoPago = canal,
                    comprobante = pago.comprobante
                )
            )
        }
        return db.withTransaction {
            compromisoDao.marcarComoPagado(compromiso.id)
            pago.copy(id = remoto.id?.toInt() ?: 0).also { pagoDao.guardar(it) }
        }
    }

    suspend fun reportarIncidencia(motivo: String, detalle: String, ahora: Long): IncidenciaEntity? {
        val usuario = _usuarioActual.value ?: return null
        val remota = llamar {
            api.crearIncidencia(
                IncidenciaRemota(usuarioId = usuario.id.toLong(), titulo = motivo, descripcion = detalle)
            )
        }
        return guardarIncidenciaRemota(remota, ahora)
    }

    suspend fun resolverIncidencia(id: Int) {
        llamar { api.actualizarEstadoIncidencia(id.toLong(), mapOf("estado" to ESTADO_REMOTO_RESUELTA)) }
        incidenciaDao.actualizarEstado(id, EstadoIncidencia.RESUELTA)
    }

    private suspend fun <T> llamar(peticion: suspend () -> Response<T>): T {
        val respuesta = peticion()
        val cuerpo = respuesta.body()
        if (!respuesta.isSuccessful || cuerpo == null) {
            val detalle = respuesta.errorBody()?.string()
                ?.let { Regex("\"error\"\\s*:\\s*\"([^\"]*)\"").find(it)?.groupValues?.get(1) }
            throw IllegalStateException(detalle ?: "Error del servidor (${respuesta.code()})")
        }
        return cuerpo
    }

    private suspend fun guardarUsuarioRemoto(remoto: UsuarioRemoto, claveHash: String? = null): UsuarioEntity {
        val id = remoto.id?.toInt() ?: throw IllegalStateException("Usuario sin identificador")
        val correo = normalizarCorreo(remoto.correo.orEmpty())
        val existente = usuarioDao.obtenerPorId(id)?.takeIf { it.correo == correo }
        val usuario = UsuarioEntity(
            id = id,
            nombre = remoto.nombre.orEmpty(),
            correo = correo,
            clave = claveHash ?: existente?.clave.orEmpty(),
            rol = remoto.rol ?: RolUsuario.CLIENTE,
            telefono = remoto.telefono ?: existente?.telefono.orEmpty(),
            direccion = existente?.direccion.orEmpty(),
            fechaRegistro = existente?.fechaRegistro ?: System.currentTimeMillis()
        )
        usuarioDao.eliminarDuplicadoPorCorreo(correo, id)
        usuarioDao.guardar(usuario)
        return usuario
    }

    private suspend fun guardarCompromisoRemoto(remoto: CompromisoRemoto): CompromisoEntity? {
        val id = remoto.id?.toInt() ?: return null
        val usuarioId = remoto.usuarioId.toInt()
        if (usuarioDao.obtenerPorId(usuarioId) == null) return null
        val totalCuotas = (remoto.cuotasTotales ?: 1).coerceAtLeast(1)
        val compromiso = CompromisoEntity(
            id = id,
            usuarioId = usuarioId,
            folio = "PF-%05d".format(id),
            organizacion = remoto.titulo,
            concepto = remoto.descripcion.orEmpty(),
            cuotaActual = ((remoto.cuotasPagadas ?: 0) + 1).coerceAtMost(totalCuotas),
            totalCuotas = totalCuotas,
            monto = (remoto.montoTotal / totalCuotas).toLong(),
            fechaVencimiento = convertirFecha(remoto.fechaVencimiento),
            estado = when (remoto.estado) {
                EstadoPago.PAGADO -> EstadoPago.PAGADO
                EstadoPago.VENCIDO -> EstadoPago.VENCIDO
                else -> EstadoPago.PENDIENTE
            }
        )
        compromisoDao.eliminarDuplicadoPorFolio(compromiso.folio, id)
        compromisoDao.guardar(compromiso)
        return compromiso
    }

    private suspend fun guardarPagoRemoto(remoto: PagoRemoto, compromiso: CompromisoEntity) {
        val id = remoto.id?.toInt() ?: return
        val existente = pagoDao.obtenerPorId(id)
        pagoDao.guardar(
            PagoEntity(
                id = id,
                compromisoId = compromiso.id,
                montoPagado = remoto.monto.toLong(),
                fechaPago = convertirFecha(remoto.fechaPago),
                rutaComprobanteFoto = existente?.rutaComprobanteFoto,
                folio = compromiso.folio,
                organizacion = compromiso.organizacion,
                concepto = compromiso.concepto,
                cuotaActual = compromiso.cuotaActual,
                totalCuotas = compromiso.totalCuotas,
                canal = remoto.metodoPago.orEmpty(),
                comprobante = remoto.comprobante ?: "PF-PAGO-API-$id"
            )
        )
    }

    private suspend fun guardarIncidenciaRemota(
        remota: IncidenciaRemota,
        fechaPorDefecto: Long = System.currentTimeMillis()
    ): IncidenciaEntity? {
        val id = remota.id?.toInt() ?: return null
        val usuarioId = remota.usuarioId.toInt()
        if (usuarioDao.obtenerPorId(usuarioId) == null) return null
        val incidencia = IncidenciaEntity(
            id = id,
            usuarioId = usuarioId,
            folio = "PF-INC-%05d".format(id),
            motivo = remota.titulo,
            detalle = remota.descripcion,
            fechaReporte = convertirFecha(remota.fechaCreacion, fechaPorDefecto),
            estado = if (remota.estado == ESTADO_REMOTO_RESUELTA || remota.estado == ESTADO_REMOTO_CERRADA) {
                EstadoIncidencia.RESUELTA
            } else {
                EstadoIncidencia.EN_REVISION
            },
            comprobante = "PF-INC-API-$id"
        )
        incidenciaDao.guardar(incidencia)
        return incidencia
    }

    private fun convertirFecha(texto: String?, porDefecto: Long = System.currentTimeMillis()): Long {
        if (texto.isNullOrBlank()) return porDefecto
        val conHora = texto.length >= 19
        val formato = SimpleDateFormat(if (conHora) "yyyy-MM-dd'T'HH:mm:ss" else "yyyy-MM-dd", Locale.US)
        return runCatching { formato.parse(texto.take(if (conHora) 19 else 10))?.time }.getOrNull() ?: porDefecto
    }

    private fun formatearFecha(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    private fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    private fun inicioDelDia(millis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private companion object {
        const val ESTADO_REMOTO_RESUELTA = "RESUELTA"
        const val ESTADO_REMOTO_CERRADA = "CERRADA"
    }
}
