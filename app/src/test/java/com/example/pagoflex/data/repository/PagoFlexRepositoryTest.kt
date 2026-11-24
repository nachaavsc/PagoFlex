package com.example.pagoflex.data.repository

import androidx.room.withTransaction
import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoDao
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.IncidenciaDao
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoDao
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.TransaccionDao
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.local.UsuarioDao
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.remote.CompromisoRemoto
import com.example.pagoflex.data.remote.IncidenciaRemota
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.remote.PagoRemoto
import com.example.pagoflex.data.remote.UsuarioRemoto
import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.LoginResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class PagoFlexRepositoryTest {

    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val transaccionDao = mockk<TransaccionDao>(relaxed = true)
    private val compromisoDao = mockk<CompromisoDao>(relaxed = true)
    private val pagoDao = mockk<PagoDao>(relaxed = true)
    private val incidenciaDao = mockk<IncidenciaDao>(relaxed = true)
    private val api = mockk<PagoFlexApi>()
    private val db = mockk<AppDatabase> {
        every { usuarioDao() } returns this@PagoFlexRepositoryTest.usuarioDao
        every { transaccionDao() } returns this@PagoFlexRepositoryTest.transaccionDao
        every { compromisoDao() } returns this@PagoFlexRepositoryTest.compromisoDao
        every { pagoDao() } returns this@PagoFlexRepositoryTest.pagoDao
        every { incidenciaDao() } returns this@PagoFlexRepositoryTest.incidenciaDao
    }
    private lateinit var repository: PagoFlexRepository

    @Before
    fun preparar() {
        coEvery { usuarioDao.obtenerPorId(any()) } returns null
        coEvery { usuarioDao.obtenerPorCorreo(any()) } returns null
        coEvery { usuarioDao.autenticar(any(), any()) } returns null
        coEvery { compromisoDao.obtenerPorId(any()) } returns null
        coEvery { pagoDao.obtenerPorId(any()) } returns null
        repository = PagoFlexRepository(db, api)
    }

    @After
    fun limpiar() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    private fun <T> errorHttp(codigo: Int, cuerpo: String = "{}"): Response<T> =
        Response.error(codigo, cuerpo.toResponseBody())

    private suspend fun iniciarSesionComo(rol: String, id: Long = 1L): UsuarioEntity? {
        coEvery { api.login(any()) } returns Response.success(LoginResponse(1L, id, "ana@pagoflex.cl", rol))
        coEvery { api.obtenerUsuario(id) } returns Response.success(
            UsuarioRemoto(id = id, nombre = "Ana Pérez", correo = " Ana@PagoFlex.cl ", telefono = "+56912345678", rol = rol)
        )
        coEvery { api.listarCompromisosPorUsuario(any()) } returns Response.success(emptyList())
        coEvery { api.listarIncidenciasPorUsuario(any()) } returns Response.success(emptyList())
        coEvery { api.listarUsuarios() } returns Response.success(emptyList())
        coEvery { api.listarCompromisos() } returns Response.success(emptyList())
        coEvery { api.listarIncidencias() } returns Response.success(emptyList())
        return repository.iniciarSesion("ana@pagoflex.cl", "Clave#2024")
    }

    @Test
    fun iniciarSesion_exitoso_guardaUsuarioConClaveHasheadaYSincroniza() = runTest {
        val usuario = iniciarSesionComo(RolUsuario.CLIENTE)

        assertEquals("ana@pagoflex.cl", usuario?.correo)
        assertEquals(Hash.sha256("Clave#2024"), usuario?.clave)
        assertEquals(usuario, repository.usuarioActual.value)
        assertTrue(repository.verificarClave("Clave#2024"))
        assertFalse(repository.verificarClave("otra"))
        coVerify { usuarioDao.guardar(usuario!!) }
        coVerify { api.listarCompromisosPorUsuario(1L) }
        coVerify { compromisoDao.marcarVencidos(any()) }

        repository.cerrarSesion()
        assertNull(repository.usuarioActual.value)
    }

    @Test
    fun iniciarSesion_conErroresHttpYSinRed_manejaCadaCaso() = runTest {
        coEvery { api.login(any()) } returns errorHttp(401)
        assertNull(repository.iniciarSesion("ana@pagoflex.cl", "mala"))

        coEvery { api.login(any()) } returns Response.success(LoginResponse(1L, 1L, "ana@pagoflex.cl", RolUsuario.CLIENTE))
        coEvery { api.obtenerUsuario(1L) } returns errorHttp(500)
        assertNull(repository.iniciarSesion("ana@pagoflex.cl", "Clave#2024"))

        val local = DatosPrueba.usuario(clave = Hash.sha256("Clave#2024"))
        coEvery { api.login(any()) } throws IOException("Sin conexión")
        coEvery { usuarioDao.autenticar("ana@pagoflex.cl", Hash.sha256("Clave#2024")) } returns local
        coEvery { api.listarCompromisosPorUsuario(any()) } throws IOException("Sin conexión")

        assertEquals(local, repository.iniciarSesion("  ANA@pagoflex.cl ", "Clave#2024"))
        assertEquals(local, repository.usuarioActual.value)
    }

    @Test
    fun sincronizar_comoGestor_guardaUsuariosCompromisosPagosEIncidencias() = runTest {
        iniciarSesionComo(RolUsuario.GESTOR, id = 9L)
        val cliente = DatosPrueba.usuario(id = 2)
        coEvery { usuarioDao.obtenerPorId(2) } returns cliente
        coEvery { api.listarUsuarios() } returns Response.success(
            listOf(UsuarioRemoto(id = 2, nombre = "Ana", correo = "ana2@pagoflex.cl"))
        )
        coEvery { api.listarCompromisos() } returns Response.success(
            listOf(
                CompromisoRemoto(
                    id = 5, usuarioId = 2, titulo = "Plaza Oriente", descripcion = "Arriendo",
                    montoTotal = 30_000.0, cuotasTotales = 3, cuotasPagadas = 1,
                    fechaVencimiento = "2030-01-15", estado = EstadoPago.VENCIDO
                ),
                CompromisoRemoto(id = null, usuarioId = 2, titulo = "Sin id", montoTotal = 1.0),
                CompromisoRemoto(id = 6, usuarioId = 77, titulo = "Sin usuario", montoTotal = 1.0)
            )
        )
        coEvery { api.listarPagos(5L) } returns Response.success(
            listOf(
                PagoRemoto(id = 40, compromisoId = 5, monto = 10_000.0, metodoPago = "Webpay", fechaPago = "2030-01-10T10:30:00"),
                PagoRemoto(id = null, compromisoId = 5, monto = 1.0)
            )
        )
        coEvery { api.listarIncidencias() } returns Response.success(
            listOf(
                IncidenciaRemota(id = 3, usuarioId = 2, titulo = "Cobro", descripcion = "Duplicado", estado = "CERRADA"),
                IncidenciaRemota(id = 4, usuarioId = 2, titulo = "Otro", descripcion = "Pendiente", estado = "ABIERTA")
            )
        )
        val compromiso = slot<CompromisoEntity>()
        val pago = slot<PagoEntity>()
        val incidencias = mutableListOf<IncidenciaEntity>()
        coEvery { compromisoDao.guardar(capture(compromiso)) } returns Unit
        coEvery { pagoDao.guardar(capture(pago)) } returns Unit
        coEvery { incidenciaDao.guardar(capture(incidencias)) } returns Unit

        assertTrue(repository.sincronizar())

        assertEquals("PF-00005", compromiso.captured.folio)
        assertEquals(10_000L, compromiso.captured.monto)
        assertEquals(2, compromiso.captured.cuotaActual)
        assertEquals(EstadoPago.VENCIDO, compromiso.captured.estado)
        assertEquals(40, pago.captured.id)
        assertEquals("PF-PAGO-API-40", pago.captured.comprobante)
        assertEquals(listOf(EstadoIncidencia.RESUELTA, EstadoIncidencia.EN_REVISION), incidencias.map { it.estado })
        coVerify(exactly = 1) { compromisoDao.guardar(any()) }
    }

    @Test
    fun sincronizar_sinSesionOConErrorHttp_retornaFalse() = runTest {
        assertFalse(repository.sincronizar())

        iniciarSesionComo(RolUsuario.CLIENTE)
        coEvery { api.listarCompromisosPorUsuario(1L) } returns errorHttp(503)

        assertFalse(repository.sincronizar())
    }

    @Test
    fun registrarUsuario_exitosoYConMensajeDeErrorDelServidor() = runTest {
        val cuerpo = slot<Map<String, String>>()
        coEvery { api.registro(capture(cuerpo)) } returns
            Response.success(LoginResponse(1L, 15L, "ana@pagoflex.cl", RolUsuario.CLIENTE))

        assertEquals(15L, repository.registrarUsuario(" Ana ", " ANA@pagoflex.cl", "+56 9 1234 5678", "Clave#2024"))
        assertEquals(
            mapOf("nombre" to "Ana", "correo" to "ana@pagoflex.cl", "clave" to Hash.sha256("Clave#2024"), "telefono" to "+56912345678"),
            cuerpo.captured
        )

        coEvery { api.registro(any()) } returns errorHttp(409, "{\"error\": \"El correo ya está registrado\"}")
        val conMensaje = runCatching { repository.registrarUsuario("Ana", "ana@pagoflex.cl", "+56912345678", "Clave#2024") }
        assertEquals("El correo ya está registrado", conMensaje.exceptionOrNull()?.message)

        coEvery { api.registro(any()) } returns errorHttp(500, "sin formato")
        val sinMensaje = runCatching { repository.registrarUsuario("Ana", "ana@pagoflex.cl", "+56912345678", "Clave#2024") }
        assertEquals("Error del servidor (500)", sinMensaje.exceptionOrNull()?.message)
    }

    @Test
    fun restablecerClave_validaTelefonoYExistenciaDelUsuario() = runTest {
        coEvery { api.obtenerUsuarioPorCorreo("ana@pagoflex.cl") } returns
            Response.success(UsuarioRemoto(id = 1, correo = "ana@pagoflex.cl", telefono = "+56912345678"))
        coEvery { api.cambiarClave(any()) } returns Response.success(LoginResponse(1L, 1L, "ana@pagoflex.cl", RolUsuario.CLIENTE))
        coEvery { usuarioDao.obtenerPorCorreo("ana@pagoflex.cl") } returns DatosPrueba.usuario(id = 1)

        assertFalse(repository.restablecerClave("ana@pagoflex.cl", "+56900000000", "Nueva#2024"))
        assertTrue(repository.restablecerClave("Ana@pagoflex.cl", "+56 9 1234 5678", "Nueva#2024"))
        coVerify { usuarioDao.actualizarClave(1, Hash.sha256("Nueva#2024")) }

        coEvery { api.obtenerUsuarioPorCorreo("nadie@pagoflex.cl") } returns errorHttp(404)
        assertFalse(repository.restablecerClave("nadie@pagoflex.cl", "+56912345678", "Nueva#2024"))
    }

    @Test
    fun cambiarClaveYActualizarPerfil_actualizanUsuarioEnSesion() = runTest {
        assertFalse(repository.cambiarClave("Clave#2024", "Nueva#2024"))
        repository.actualizarPerfil("Ana", "ana@pagoflex.cl", "+56912345678", "Centro")
        coVerify(exactly = 0) { api.actualizarUsuario(any(), any()) }

        iniciarSesionComo(RolUsuario.CLIENTE)
        coEvery { api.cambiarClave(any()) } returns Response.success(LoginResponse(1L, 1L, "ana@pagoflex.cl", RolUsuario.CLIENTE))
        coEvery { api.actualizarUsuario(1L, any()) } returns Response.success(UsuarioRemoto(id = 1))

        assertFalse(repository.cambiarClave("incorrecta", "Nueva#2024"))
        assertTrue(repository.cambiarClave("Clave#2024", "Nueva#2024"))
        assertTrue(repository.verificarClave("Nueva#2024"))

        repository.actualizarPerfil(" Ana María ", " ANA@pagoflex.cl ", "+56 9 8765 4321", " Los Leones 45 ")
        val actual = repository.usuarioActual.value
        assertEquals("Ana María", actual?.nombre)
        assertEquals("+56987654321", actual?.telefono)
        assertEquals("Los Leones 45", actual?.direccion)
        coVerify { usuarioDao.actualizar(actual!!) }
    }

    @Test
    fun registrarCompromiso_calculaEstadoYFallaSiElServidorNoEntregaId() = runTest {
        val enviado = slot<CompromisoRemoto>()
        coEvery { usuarioDao.obtenerPorId(3) } returns DatosPrueba.usuario(id = 3)
        coEvery { api.crearCompromiso(capture(enviado)) } answers {
            Response.success(enviado.captured.copy(id = 11))
        }

        val vencido = repository.registrarCompromiso(3, " Plaza ", " Arriendo ", 50_000L, 0L)

        assertEquals(EstadoPago.VENCIDO, enviado.captured.estado)
        assertTrue(enviado.captured.fechaVencimiento.orEmpty().matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertEquals("PF-00011", vencido.folio)
        assertEquals("Plaza", vencido.organizacion)

        repository.registrarCompromiso(3, "Plaza", "Arriendo", 50_000L, System.currentTimeMillis() + 86_400_000L * 30)
        assertEquals(EstadoPago.PENDIENTE, enviado.captured.estado)

        coEvery { api.crearCompromiso(any()) } answers { Response.success(enviado.captured.copy(id = null)) }
        val fallo = runCatching { repository.registrarCompromiso(3, "Plaza", "Arriendo", 50_000L, 0L) }
        assertEquals("No se pudo guardar el compromiso", fallo.exceptionOrNull()?.message)
    }

    @Test
    fun registrarPago_marcaCompromisoPagadoYGuardaPagoConIdRemoto() = runTest {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction(any<suspend () -> PagoEntity>()) } coAnswers {
            secondArg<suspend () -> PagoEntity>().invoke()
        }
        val compromiso = DatosPrueba.compromiso(id = 5)
        coEvery { api.registrarPago(5L, any()) } returns
            Response.success(PagoRemoto(id = 77, compromisoId = 5, monto = 10_000.0))

        val pago = repository.registrarPago(compromiso, "Webpay", 1_000L)

        assertEquals(77, pago.id)
        assertEquals("PF-PAGO-1000", pago.comprobante)
        coVerify { compromisoDao.marcarComoPagado(5) }
        coVerify { pagoDao.guardar(pago) }

        coEvery { api.registrarPago(5L, any()) } returns errorHttp(500)
        assertTrue(runCatching { repository.registrarPago(compromiso, "Webpay", 2_000L) }.isFailure)
    }

    @Test
    fun incidenciasYConsultasLocales_deleganEnDaoYApi() = runTest {
        assertNull(repository.reportarIncidencia("Cobro", "Detalle", 0L))

        iniciarSesionComo(RolUsuario.CLIENTE)
        coEvery { usuarioDao.obtenerPorId(1) } returns DatosPrueba.usuario(id = 1)
        coEvery { api.crearIncidencia(any()) } returns Response.success(
            IncidenciaRemota(id = 8, usuarioId = 1, titulo = "Cobro", descripcion = "Detalle", fechaCreacion = "fecha-invalida")
        )
        coEvery { api.actualizarEstadoIncidencia(8L, mapOf("estado" to "RESUELTA")) } returns
            Response.success(IncidenciaRemota(id = 8, usuarioId = 1, titulo = "Cobro", descripcion = "Detalle"))

        val incidencia = repository.reportarIncidencia("Cobro", "Detalle", 123L)
        assertEquals("PF-INC-00008", incidencia?.folio)
        assertEquals(123L, incidencia?.fechaReporte)

        repository.resolverIncidencia(8)
        coVerify { incidenciaDao.actualizarEstado(8, EstadoIncidencia.RESUELTA) }

        coEvery { usuarioDao.obtenerPorCorreo("ana@pagoflex.cl") } returns DatosPrueba.usuario(id = 1)
        assertTrue(repository.existeCorreo(" ANA@pagoflex.cl "))
        assertFalse(repository.existeCorreo("ana@pagoflex.cl", excluirId = 1))
        assertFalse(repository.existeCorreo("libre@pagoflex.cl"))

        val compromiso = DatosPrueba.compromiso(id = 4)
        coEvery { compromisoDao.obtenerPorId(4) } returns compromiso
        assertEquals(compromiso, repository.obtenerCompromiso(4))

        val transaccion = TransaccionEntity(concepto = "Pago", monto = 1.0)
        repository.agregarTransaccion(transaccion)
        coVerify { transaccionDao.insertarTransaccion(transaccion) }

        repository.compromisosDeUsuario(1)
        repository.historialDeUsuario(1)
        repository.incidenciasDeUsuario(1)
        repository.observarUsuario(1)
        coVerify { compromisoDao.observarPorUsuario(1) }
        coVerify { pagoDao.observarHistorialPorUsuario(1) }
        coVerify { incidenciaDao.observarPorUsuario(1) }
        coVerify { usuarioDao.observarPorId(1) }
    }
}
