package com.example.pagoflex.viewmodel

import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.DatosPrueba
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.data.repository.ReglaCorrutinas
import com.example.pagoflex.notificaciones.GestorNotificaciones
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CompromisosViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@CompromisosViewModelTest.usuarioActual
        coEvery { sincronizar() } returns true
    }
    private val notificaciones = mockk<GestorNotificaciones>(relaxed = true)

    private fun TestScope.crearViewModel() = CompromisosViewModel(repository).also { viewModel ->
        backgroundScope.launch { viewModel.compromisos.collect {} }
        backgroundScope.launch { viewModel.conteoPorEstado.collect {} }
        backgroundScope.launch { viewModel.esGestor.collect {} }
    }

    @Test
    fun gestor_veCompromisosConNombreDelClienteYPuedeFiltrar() = runTest(regla.dispatcher) {
        every { repository.compromisos } returns flowOf(
            listOf(
                DatosPrueba.compromiso(id = 1, usuarioId = 1, estado = EstadoPago.PENDIENTE),
                DatosPrueba.compromiso(id = 2, usuarioId = 2, estado = EstadoPago.PAGADO),
                DatosPrueba.compromiso(id = 3, usuarioId = 3, estado = EstadoPago.PENDIENTE)
            )
        )
        every { repository.clientes } returns flowOf(
            listOf(DatosPrueba.usuario(id = 1, nombre = "Ana"), DatosPrueba.usuario(id = 2, nombre = "Luis"))
        )
        usuarioActual.value = DatosPrueba.usuario(id = 9, rol = RolUsuario.GESTOR)

        val viewModel = crearViewModel()

        assertTrue(viewModel.esGestor.value)
        assertEquals(listOf("Ana", "Luis", null), viewModel.compromisos.value.map { it.nombreCliente })
        assertEquals(mapOf(EstadoPago.PENDIENTE to 2, EstadoPago.PAGADO to 1), viewModel.conteoPorEstado.value)

        viewModel.onFiltroChange(EstadoPago.PAGADO)

        assertEquals(EstadoPago.PAGADO, viewModel.filtroEstado.value)
        assertEquals(listOf(2), viewModel.compromisos.value.map { it.compromiso.id })
    }

    @Test
    fun cliente_veSoloSusCompromisosYSinSesionNoVeNada() = runTest(regla.dispatcher) {
        every { repository.compromisosDeUsuario(1) } returns flowOf(listOf(DatosPrueba.compromiso(id = 5)))
        usuarioActual.value = DatosPrueba.usuario(id = 1)

        val viewModel = crearViewModel()

        assertFalse(viewModel.esGestor.value)
        assertEquals(1, viewModel.compromisos.value.size)
        assertNull(viewModel.compromisos.value.first().nombreCliente)

        usuarioActual.value = null

        assertTrue(viewModel.compromisos.value.isEmpty())
        assertTrue(viewModel.conteoPorEstado.value.isEmpty())
    }

    @Test
    fun confirmarPago_exitoso_marcaPagadoNotificaYEmiteExito() = runTest(regla.dispatcher) {
        usuarioActual.value = DatosPrueba.usuario(id = 1)
        val compromiso = DatosPrueba.compromiso(id = 1, usuarioId = 1)
        val pago = DatosPrueba.pago(compromisoId = 1)
        coEvery { repository.obtenerCompromiso(1) } returns compromiso
        coEvery { repository.registrarPago(compromiso, "App Móvil PagoFlex", any()) } returns pago
        val viewModel = ConfirmarPagoViewModel(repository, notificaciones)
        viewModel.cargar(1)

        viewModel.confirmarPago()
        viewModel.confirmarPago()

        val estado = viewModel.uiState.value
        assertEquals(EstadoPago.PAGADO, estado.compromiso?.estado)
        assertEquals(pago, estado.pagoRegistrado)
        assertFalse(estado.cargando)
        verify(exactly = 1) { notificaciones.notificarPago(pago) }
        assertEquals(EventoUI.Exito("Pago registrado con éxito"), viewModel.eventos.first())
    }

    @Test
    fun confirmarPago_conFalloDeRed_emiteErrorYLiberaCarga() = runTest(regla.dispatcher) {
        usuarioActual.value = DatosPrueba.usuario(id = 1)
        coEvery { repository.obtenerCompromiso(1) } returns DatosPrueba.compromiso(id = 1, usuarioId = 1)
        coEvery { repository.registrarPago(any(), any(), any()) } throws IllegalStateException("Error del servidor (500)")
        val viewModel = ConfirmarPagoViewModel(repository, notificaciones)
        viewModel.cargar(1)

        viewModel.confirmarPago()

        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.pagoRegistrado)
        assertEquals(EventoUI.Error("No se pudo registrar el pago. Intenta nuevamente."), viewModel.eventos.first())
    }
}
