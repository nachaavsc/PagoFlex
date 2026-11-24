package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.remote.IndicadorRemoto
import com.example.pagoflex.data.remote.IndicadoresRemotos
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.repository.PagoFlexRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun sinSesion_entregaEstadoInicialYSincroniza() = runTest(regla.dispatcher) {
        val viewModel = HomeViewModel(repository, indicadoresApi)
        backgroundScope.launch { viewModel.uiState.collect {} }

        assertEquals(HomeUiState(), viewModel.uiState.value)
        assertFalse(viewModel.uiState.value.esGestor)
        coVerify { repository.sincronizar() }
    }

    @Test
    fun cliente_calculaTotalesVencimientosYCumplimiento() = runTest(regla.dispatcher) {
        val compromisos = listOf(
            DatosPrueba.compromiso(id = 1, estado = EstadoPago.PAGADO, fechaVencimiento = 100L),
            DatosPrueba.compromiso(id = 2, estado = EstadoPago.PENDIENTE, monto = 20_000L, fechaVencimiento = 500L),
            DatosPrueba.compromiso(id = 3, estado = EstadoPago.PENDIENTE, monto = 5_000L, fechaVencimiento = 300L),
            DatosPrueba.compromiso(id = 4, estado = EstadoPago.VENCIDO, monto = 1_000L, fechaVencimiento = 50L)
        )
        every { repository.compromisosDeUsuario(1) } returns flowOf(compromisos)
        every { repository.historialDeUsuario(1) } returns flowOf(listOf(DatosPrueba.pago(compromisoId = 1, fechaPago = 90L)))
        usuarioActual.value = DatosPrueba.usuario()

        val viewModel = HomeViewModel(repository, indicadoresApi)
        backgroundScope.launch { viewModel.uiState.collect {} }

        val estado = viewModel.uiState.value
        assertEquals("Ana Pérez", estado.nombreUsuario)
        assertEquals(26_000L, estado.totalPorPagar)
        assertEquals(300L, estado.proximoVencimiento)
        assertEquals(1, estado.cantidadVencidos)
        assertEquals(3, estado.cuentasPorPagar.size)
        assertEquals(25, estado.porcentajeCumplimiento)
        assertEquals(1, estado.pagosRealizados)
        assertEquals(4, estado.totalCompromisos)
        assertEquals(1, estado.rachaAlDia)
        assertEquals(usuarioActual.value, viewModel.usuarioActual.value)
    }

    @Test
    fun gestor_resumeRecaudacionClientesEIncidenciasAbiertas() = runTest(regla.dispatcher) {
        every { repository.compromisos } returns flowOf(
            listOf(
                DatosPrueba.compromiso(id = 1, estado = EstadoPago.PAGADO, monto = 30_000L),
                DatosPrueba.compromiso(id = 2, estado = EstadoPago.VENCIDO, monto = 4_000L),
                DatosPrueba.compromiso(id = 3, estado = EstadoPago.PENDIENTE, monto = 6_000L)
            )
        )
        every { repository.clientes } returns flowOf(listOf(DatosPrueba.usuario(id = 1), DatosPrueba.usuario(id = 2)))
        every { repository.incidencias } returns flowOf(
            listOf(
                DatosPrueba.incidencia(id = 1),
                DatosPrueba.incidencia(id = 2, estado = EstadoIncidencia.RESUELTA)
            )
        )
        usuarioActual.value = DatosPrueba.usuario(id = 9, nombre = "Gestor", rol = RolUsuario.GESTOR)

        val viewModel = HomeViewModel(repository, indicadoresApi)
        backgroundScope.launch { viewModel.uiState.collect {} }

        val estado = viewModel.uiState.value
        assertTrue(estado.esGestor)
        assertEquals(10_000L, estado.totalPorPagar)
        assertEquals(30_000L, estado.montoRecaudado)
        assertEquals(1, estado.cantidadVencidos)
        assertEquals(2, estado.totalClientes)
        assertEquals(1, estado.incidenciasAbiertas)
        assertEquals(33, estado.porcentajeCumplimiento)
    }

    @Test
    fun indicadores_cargaExitosa_exponeUfYDolar() = runTest(regla.dispatcher) {
        val viewModel = HomeViewModel(repository, indicadoresApi)

        val indicadores = viewModel.indicadores.value
        assertEquals(39_123.45, indicadores.uf!!, 0.0)
        assertEquals(948.7, indicadores.dolar!!, 0.0)
        assertFalse(indicadores.cargando)
        assertFalse(indicadores.error)
    }

    @Test
    fun indicadores_sinInternet_marcaErrorYNoAfectaResumenDelCliente() = runTest(regla.dispatcher) {
        coEvery { indicadoresApi.obtenerIndicadores() } throws IOException("Sin conexión")
        every { repository.compromisosDeUsuario(1) } returns flowOf(listOf(DatosPrueba.compromiso(id = 1, monto = 8_000L)))
        every { repository.historialDeUsuario(1) } returns flowOf(emptyList())
        usuarioActual.value = DatosPrueba.usuario()

        val viewModel = HomeViewModel(repository, indicadoresApi)
        backgroundScope.launch { viewModel.uiState.collect {} }

        val indicadores = viewModel.indicadores.value
        assertTrue(indicadores.error)
        assertFalse(indicadores.cargando)
        assertNull(indicadores.uf)
        assertNull(indicadores.dolar)
        assertEquals(8_000L, viewModel.uiState.value.totalPorPagar)
        coVerify { repository.sincronizar() }
    }

    @Test
    fun indicadores_conErrorHttp_marcaError() = runTest(regla.dispatcher) {
        coEvery { indicadoresApi.obtenerIndicadores() } returns Response.error(503, "{}".toResponseBody())

        val viewModel = HomeViewModel(repository, indicadoresApi)

        assertTrue(viewModel.indicadores.value.error)
        assertNull(viewModel.indicadores.value.uf)
    }
}
