package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun cliente_veSoloSusCompromisosSinNombre() = runTest(regla.dispatcher) {
        every { repository.compromisosDeUsuario(1) } returns flowOf(listOf(DatosPrueba.compromiso(id = 5)))
        usuarioActual.value = DatosPrueba.usuario(id = 1)

        val viewModel = crearViewModel()

        assertFalse(viewModel.esGestor.value)
        assertEquals(1, viewModel.compromisos.value.size)
        assertNull(viewModel.compromisos.value.first().nombreCliente)
    }

    @Test
    fun sinSesion_noMuestraCompromisos() = runTest(regla.dispatcher) {
        val viewModel = crearViewModel()

        assertTrue(viewModel.compromisos.value.isEmpty())
        assertTrue(viewModel.conteoPorEstado.value.isEmpty())
    }
}
