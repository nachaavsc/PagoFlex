package com.example.pagoflex.data.repository

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
import com.example.pagoflex.data.local.UsuarioDao
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.remote.CompromisoRemoto
import com.example.pagoflex.data.remote.IncidenciaRemota
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.remote.PagoRemoto
import com.example.pagoflex.data.remote.UsuarioRemoto
import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.LoginResponse
import com.example.pagoflex.model.Validador
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ReglaCorrutinas(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

object DatosPrueba {

    fun usuario(
        id: Int = 1,
        nombre: String = "Ana Pérez",
        rol: String = RolUsuario.CLIENTE,
        clave: String = ""
    ) = UsuarioEntity(
        id = id,
        nombre = nombre,
        correo = "ana$id@pagoflex.cl",
        clave = clave,
        rol = rol,
        telefono = "+56912345678",
        direccion = "Av. Siempre Viva 123",
        fechaRegistro = 0L
    )

    fun compromiso(
        id: Int = 1,
        usuarioId: Int = 1,
        estado: String = EstadoPago.PENDIENTE,
        monto: Long = 10_000L,
        fechaVencimiento: Long = 1_000_000L
    ) = CompromisoEntity(
        id = id,
        usuarioId = usuarioId,
        folio = "PF-%05d".format(id),
        organizacion = "Plaza Oriente",
        concepto = "Arriendo",
        cuotaActual = 1,
        totalCuotas = 1,
        monto = monto,
        fechaVencimiento = fechaVencimiento,
        estado = estado
    )

    fun pago(
        id: Int = 1,
        compromisoId: Int = 1,
        fechaPago: Long = System.currentTimeMillis()
    ) = PagoEntity(
        id = id,
        compromisoId = compromisoId,
        montoPagado = 10_000L,
        fechaPago = fechaPago,
        folio = "PF-%05d".format(compromisoId),
        organizacion = "Plaza Oriente",
        concepto = "Arriendo",
        cuotaActual = 1,
        totalCuotas = 1,
        canal = "App Móvil PagoFlex",
        comprobante = "PF-PAGO-$id"
    )

    fun incidencia(
        id: Int = 1,
        usuarioId: Int = 1,
        estado: String = EstadoIncidencia.EN_REVISION
    ) = IncidenciaEntity(
        id = id,
        usuarioId = usuarioId,
        folio = "PF-INC-%05d".format(id),
        motivo = "Cobro duplicado",
        detalle = "Se cobró dos veces la misma cuota",
        fechaReporte = 0L,
        estado = estado,
        comprobante = "PF-INC-API-$id"
    )
}

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
    fun sincronizar_comoGestor_guardaDatosRemotosYFallaConErrorHttp() = runTest {
        assertFalse(repository.sincronizar())

        iniciarSesionComo(RolUsuario.GESTOR, id = 9L)
        coEvery { usuarioDao.obtenerPorId(2) } returns DatosPrueba.usuario(id = 2)
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

        coEvery { api.listarUsuarios() } returns errorHttp(503)
        assertFalse(repository.sincronizar())
    }

    @Test
    fun validador_rechazaDatosInvalidosYHashEsDeterministico() {
        assertEquals("El correo es obligatorio", Validador.correo(""))
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", Validador.correo("nombre@dominio"))
        assertNull(Validador.correo("camila.rojas@pagoflex.cl"))
        assertEquals("El monto debe ser mayor a 0", Validador.monto("0"))
        assertEquals("El monto no puede superar \$10.000.000", Validador.monto("10000001"))
        assertNull(Validador.monto("10000000"))
        assertEquals("La clave debe tener al menos 8 caracteres", Validador.clave("Ab1#"))
        assertEquals("La clave debe incluir al menos un carácter especial", Validador.clave("Abcdefg1"))
        assertNull(Validador.clave("Cliente#2026"))
        assertEquals("Las claves no coinciden", Validador.confirmarClave("Cliente#2026", "Cliente#2025"))
        assertEquals("El nombre debe tener al menos 3 caracteres", Validador.nombre("Al"))
        assertEquals("Ingresa un teléfono válido, por ejemplo +56912345678", Validador.telefono("12ab"))
        assertNull(Validador.telefono("+56 9 1234 5678"))
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Hash.sha256("abc")
        )
    }
}
