package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class ConfirmarPagoViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(DatosPrueba.usuario(id = 1))
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@ConfirmarPagoViewModelTest.usuarioActual
    }
    private val notificaciones = mockk<GestorNotificaciones>(relaxed = true)
    private val viewModel by lazy { ConfirmarPagoViewModel(repository, notificaciones) }

    @Test
    fun cargar_compromisoDelUsuario_loDejaEnElEstadoUnaSolaVez() = runTest(regla.dispatcher) {
        coEvery { repository.obtenerCompromiso(1) } returns DatosPrueba.compromiso(id = 1, usuarioId = 1)

        viewModel.cargar(1)
        viewModel.cargar(1)

        assertEquals(1, viewModel.uiState.value.compromiso?.id)
        coVerify(exactly = 1) { repository.obtenerCompromiso(1) }
    }

    @Test
    fun cargar_compromisoDeOtroUsuario_emiteError() = runTest(regla.dispatcher) {
        coEvery { repository.obtenerCompromiso(2) } returns DatosPrueba.compromiso(id = 2, usuarioId = 99)

        viewModel.cargar(2)

        assertNull(viewModel.uiState.value.compromiso)
        assertEquals(EventoUI.Error("No encontramos el compromiso seleccionado"), viewModel.eventos.first())
    }

    @Test
    fun confirmarPago_exitoso_marcaPagadoNotificaYEmiteExito() = runTest(regla.dispatcher) {
        val compromiso = DatosPrueba.compromiso(id = 1, usuarioId = 1)
        val pago = DatosPrueba.pago(compromisoId = 1)
        coEvery { repository.obtenerCompromiso(1) } returns compromiso
        coEvery { repository.registrarPago(compromiso, "App Móvil PagoFlex", any()) } returns pago
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
        coEvery { repository.obtenerCompromiso(1) } returns DatosPrueba.compromiso(id = 1, usuarioId = 1)
        coEvery { repository.registrarPago(any(), any(), any()) } throws IllegalStateException("Error del servidor (500)")
        viewModel.cargar(1)

        viewModel.confirmarPago()

        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.pagoRegistrado)
        assertEquals(EventoUI.Error("No se pudo registrar el pago. Intenta nuevamente."), viewModel.eventos.first())
    }

    @Test
    fun confirmarPago_compromisoYaPagado_noRegistraNuevamente() = runTest(regla.dispatcher) {
        coEvery { repository.obtenerCompromiso(1) } returns
            DatosPrueba.compromiso(id = 1, usuarioId = 1, estado = EstadoPago.PAGADO)
        viewModel.confirmarPago()
        viewModel.cargar(1)

        viewModel.confirmarPago()

        assertEquals(EventoUI.Error("Este compromiso ya fue pagado"), viewModel.eventos.first())
        coVerify(exactly = 0) { repository.registrarPago(any(), any(), any()) }
    }
}
