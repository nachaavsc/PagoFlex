package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.IncidenciaUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

class IncidenciasViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@IncidenciasViewModelTest.usuarioActual
        coEvery { sincronizar() } returns true
    }

    private fun TestScope.crearViewModel() = IncidenciasViewModel(repository).also { viewModel ->
        backgroundScope.launch { viewModel.incidencias.collect {} }
        backgroundScope.launch { viewModel.esGestor.collect {} }
    }

    private fun IncidenciasViewModel.completarFormulario() {
        onMotivoChange("Cobro duplicado")
        onDetalleChange("  Se cobró dos veces la cuota  ")
    }

    @Test
    fun listado_gestorVeNombresYClienteSoloLasPropias() = runTest(regla.dispatcher) {
        every { repository.incidencias } returns flowOf(
            listOf(DatosPrueba.incidencia(id = 1, usuarioId = 1), DatosPrueba.incidencia(id = 2, usuarioId = 5))
        )
        every { repository.clientes } returns flowOf(listOf(DatosPrueba.usuario(id = 1, nombre = "Ana")))
        every { repository.incidenciasDeUsuario(1) } returns flowOf(listOf(DatosPrueba.incidencia(id = 3)))
        usuarioActual.value = DatosPrueba.usuario(id = 9, rol = RolUsuario.GESTOR)
        val viewModel = crearViewModel()

        assertTrue(viewModel.esGestor.value)
        assertEquals(listOf("Ana", null), viewModel.incidencias.value.map { it.nombreCliente })

        usuarioActual.value = DatosPrueba.usuario(id = 1)

        assertFalse(viewModel.esGestor.value)
        assertEquals(listOf(3), viewModel.incidencias.value.map { it.incidencia.id })

        usuarioActual.value = null
        assertTrue(viewModel.incidencias.value.isEmpty())
    }

    @Test
    fun reportar_conCamposInvalidos_marcaErroresYEmiteAviso() = runTest(regla.dispatcher) {
        val viewModel = crearViewModel()
        viewModel.onDetalleChange("corto")
        assertEquals("La descripción debe tener al menos 10 caracteres", viewModel.uiState.value.detalleError)

        viewModel.onDetalleChange("")
        viewModel.reportarIncidencia()

        assertEquals("Selecciona el motivo de la incidencia", viewModel.uiState.value.motivoError)
        assertEquals("Describe lo que ocurrió", viewModel.uiState.value.detalleError)
        assertEquals(EventoUI.Error("Revisa los campos marcados"), viewModel.eventos.first())
        coVerify(exactly = 0) { repository.reportarIncidencia(any(), any(), any()) }
    }

    @Test
    fun reportar_exitoso_limpiaFormularioYEmiteFolio() = runTest(regla.dispatcher) {
        coEvery { repository.reportarIncidencia("Cobro duplicado", "Se cobró dos veces la cuota", any()) } returns
            DatosPrueba.incidencia(id = 12)
        val viewModel = crearViewModel()
        viewModel.completarFormulario()

        viewModel.reportarIncidencia()

        assertEquals(IncidenciaUIState(), viewModel.uiState.value)
        assertEquals(
            EventoUI.Exito("Incidencia PF-INC-00012 enviada. Te avisaremos cuando sea revisada."),
            viewModel.eventos.first()
        )
    }

    @Test
    fun reportar_sinSesionOConErrorDeServidor_emiteError() = runTest(regla.dispatcher) {
        coEvery { repository.reportarIncidencia(any(), any(), any()) } returns null andThenThrows
            IllegalStateException("Error del servidor (503)")
        val viewModel = crearViewModel()
        viewModel.completarFormulario()

        viewModel.reportarIncidencia()
        assertEquals(EventoUI.Error("Tu sesión expiró. Inicia sesión nuevamente."), viewModel.eventos.first())

        viewModel.reportarIncidencia()
        assertEquals(EventoUI.Error("Error del servidor (503)"), viewModel.eventos.first())
        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.motivoError)
    }

    @Test
    fun resolverIncidencia_informaExitoYError() = runTest(regla.dispatcher) {
        val incidencia = DatosPrueba.incidencia(id = 4)
        coEvery { repository.resolverIncidencia(4) } returns Unit andThenThrows IllegalStateException("Sin conexión")
        val viewModel = crearViewModel()

        viewModel.resolverIncidencia(incidencia)
        assertEquals(EventoUI.Exito("Incidencia PF-INC-00004 marcada como resuelta"), viewModel.eventos.first())

        viewModel.resolverIncidencia(incidencia)
        assertEquals(EventoUI.Error("Sin conexión"), viewModel.eventos.first())
    }
}
