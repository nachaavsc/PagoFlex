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
import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    suspend fun iniciarSesion(correo: String, clave: String): UsuarioEntity? =
        usuarioDao.autenticar(normalizarCorreo(correo), Hash.sha256(clave))
            .also { _usuarioActual.value = it }

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

    private fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

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
