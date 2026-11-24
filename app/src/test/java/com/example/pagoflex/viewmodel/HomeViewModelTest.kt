package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.remote.IndicadorRemoto
import com.example.pagoflex.data.remote.IndicadoresRemotos
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.remote.RetrofitClient
import com.example.pagoflex.data.repository.DatosPrueba
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.data.repository.ReglaCorrutinas
import com.example.pagoflex.model.IncidenciaUIState
import com.example.pagoflex.notificaciones.GestorNotificaciones
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class HomeViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@HomeViewModelTest.usuarioActual
        coEvery { sincronizar() } returns true
    }
    private val indicadoresApi = mockk<PagoFlexApi> {
        coEvery { obtenerIndicadores() } returns Response.success(
            IndicadoresRemotos(uf = IndicadorRemoto(codigo = "uf", valor = 39_123.45), dolar = IndicadorRemoto(codigo = "dolar", valor = 948.7))
        )
    }

    @Before
    fun preparar() {
        mockkObject(RetrofitClient)
        every { RetrofitClient.indicadoresApi } returns indicadoresApi
    }

    @After
    fun limpiar() {
        unmockkObject(RetrofitClient)
    }

    @Test
    fun cliente_calculaTotalesVencimientosCumplimientoEIndicadores() = runTest(regla.dispatcher) {
        val compromisos = listOf(
            DatosPrueba.compromiso(id = 1, estado = EstadoPago.PAGADO, fechaVencimiento = 100L),
            DatosPrueba.compromiso(id = 2, estado = EstadoPago.PENDIENTE, monto = 20_000L, fechaVencimiento = 500L),
            DatosPrueba.compromiso(id = 3, estado = EstadoPago.PENDIENTE, monto = 5_000L, fechaVencimiento = 300L),
            DatosPrueba.compromiso(id = 4, estado = EstadoPago.VENCIDO, monto = 1_000L, fechaVencimiento = 50L)
        )
        every { repository.compromisosDeUsuario(1) } returns flowOf(compromisos)
        every { repository.historialDeUsuario(1) } returns flowOf(listOf(DatosPrueba.pago(compromisoId = 1, fechaPago = 90L)))
        usuarioActual.value = DatosPrueba.usuario()

        val viewModel = HomeViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        val estado = viewModel.uiState.value
        assertEquals("Ana Pérez", estado.nombreUsuario)
        assertEquals(26_000L, estado.totalPorPagar)
        assertEquals(300L, estado.proximoVencimiento)
        assertEquals(1, estado.cantidadVencidos)
        assertEquals(25, estado.porcentajeCumplimiento)
        assertEquals(1, estado.rachaAlDia)
        assertEquals(39_123.45, viewModel.indicadores.value.uf!!, 0.0)
        assertEquals(948.7, viewModel.indicadores.value.dolar!!, 0.0)
        assertFalse(viewModel.indicadores.value.error)
        coVerify { repository.sincronizar() }
    }

    @Test
    fun indicadores_sinInternet_marcaErrorYNoAfectaResumenDelCliente() = runTest(regla.dispatcher) {
        coEvery { indicadoresApi.obtenerIndicadores() } throws IOException("Sin conexión")
        every { repository.compromisosDeUsuario(1) } returns flowOf(listOf(DatosPrueba.compromiso(id = 1, monto = 8_000L)))
        every { repository.historialDeUsuario(1) } returns flowOf(emptyList())
        usuarioActual.value = DatosPrueba.usuario()

        val viewModel = HomeViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        val indicadores = viewModel.indicadores.value
        assertTrue(indicadores.error)
        assertFalse(indicadores.cargando)
        assertNull(indicadores.uf)
        assertNull(indicadores.dolar)
        assertEquals(8_000L, viewModel.uiState.value.totalPorPagar)
    }

    @Test
    fun fabrica_creaHomeViewModelConRetrofitClientSimuladoYRechazaNoSoportados() {
        val fabrica = PagoFlexViewModelFactory(
            repository = repository,
            notificaciones = mockk<GestorNotificaciones>(relaxed = true),
            almacenImagenes = mockk<AlmacenImagenes>(relaxed = true)
        )

        val home = fabrica.create(HomeViewModel::class.java)

        assertEquals(39_123.45, home.indicadores.value.uf!!, 0.0)
        coVerify { indicadoresApi.obtenerIndicadores() }
        val error = runCatching { fabrica.create(ViewModelDesconocido::class.java) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    private class ViewModelDesconocido : ViewModel()

    @Test
    fun incidencias_reportarExitosoYConErrorDeServidor() = runTest(regla.dispatcher) {
        coEvery { repository.reportarIncidencia("Cobro duplicado", "Se cobró dos veces la cuota", any()) } returns
            DatosPrueba.incidencia(id = 12) andThenThrows IllegalStateException("Error del servidor (503)")
        val viewModel = IncidenciasViewModel(repository)

        viewModel.onMotivoChange("Cobro duplicado")
        viewModel.onDetalleChange("  Se cobró dos veces la cuota  ")
        viewModel.reportarIncidencia()

        assertEquals(IncidenciaUIState(), viewModel.uiState.value)
        assertEquals(
            EventoUI.Exito("Incidencia PF-INC-00012 enviada. Te avisaremos cuando sea revisada."),
            viewModel.eventos.first()
        )

        viewModel.onMotivoChange("Cobro duplicado")
        viewModel.onDetalleChange("  Se cobró dos veces la cuota  ")
        viewModel.reportarIncidencia()

        assertEquals(EventoUI.Error("Error del servidor (503)"), viewModel.eventos.first())
        assertFalse(viewModel.uiState.value.cargando)
    }
}
