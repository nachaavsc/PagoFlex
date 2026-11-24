package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.NuevoCompromisoUIState
import com.example.pagoflex.notificaciones.GestorNotificaciones
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class NuevoCompromisoViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val cliente = DatosPrueba.usuario(id = 3, nombre = "Luis Soto")
    private val repository = mockk<PagoFlexRepository> {
        every { clientes } returns flowOf(listOf(cliente))
    }
    private val notificaciones = mockk<GestorNotificaciones>(relaxed = true)
    private val viewModel by lazy { NuevoCompromisoViewModel(repository, notificaciones) }

    private fun completarFormulario() {
        viewModel.onClienteChange(cliente)
        viewModel.onOrganizacionChange("Plaza Oriente")
        viewModel.onConceptoChange("Arriendo")
        viewModel.onMontoChange("150000")
        viewModel.onFechaVencimientoChange(5_000L)
    }

    @Test
    fun guardar_sinDatos_marcaTodosLosErroresYEmiteAviso() = runTest(regla.dispatcher) {
        backgroundScope.launch { viewModel.clientes.collect {} }
        assertEquals(listOf(cliente), viewModel.clientes.value)

        viewModel.onMontoChange("0")
        assertEquals("El monto debe ser mayor a 0", viewModel.uiState.value.montoError)
        viewModel.onMontoChange("")
        viewModel.guardarCompromiso()

        val estado = viewModel.uiState.value
        assertEquals("Selecciona el cliente", estado.clienteError)
        assertEquals("La organización es obligatoria", estado.organizacionError)
        assertEquals("El concepto es obligatorio", estado.conceptoError)
        assertEquals("El monto es obligatorio", estado.montoError)
        assertEquals("Selecciona la fecha de vencimiento", estado.fechaError)
        assertEquals(EventoUI.Error("Revisa los campos marcados"), viewModel.eventos.first())
        coVerify(exactly = 0) { repository.registrarCompromiso(any(), any(), any(), any(), any()) }
    }

    @Test
    fun guardar_exitoso_notificaReiniciaFormularioYEmiteExito() = runTest(regla.dispatcher) {
        val compromiso = DatosPrueba.compromiso(id = 8, usuarioId = 3)
        coEvery { repository.registrarCompromiso(3, "Plaza Oriente", "Arriendo", 150_000L, 5_000L) } returns compromiso
        completarFormulario()

        viewModel.guardarCompromiso()

        assertEquals(NuevoCompromisoUIState(), viewModel.uiState.value)
        verify { notificaciones.notificarCompromisoAsignado(compromiso, "Luis Soto") }
        assertEquals(EventoUI.Exito("Compromiso PF-00008 asignado a Luis Soto"), viewModel.eventos.first())
    }

    @Test
    fun guardar_conErrorDelServidor_conservaDatosYEmiteMensaje() = runTest(regla.dispatcher) {
        coEvery { repository.registrarCompromiso(any(), any(), any(), any(), any()) } throws
            IllegalStateException("Cliente no encontrado")
        completarFormulario()

        viewModel.guardarCompromiso()

        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals("Plaza Oriente", estado.organizacion)
        assertNull(estado.clienteError)
        assertEquals(EventoUI.Error("Cliente no encontrado"), viewModel.eventos.first())
        verify(exactly = 0) { notificaciones.notificarCompromisoAsignado(any(), any()) }
    }
}
