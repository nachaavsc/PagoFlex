package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class HistorialViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val ahora = System.currentTimeMillis()
    private val dia = TimeUnit.DAYS.toMillis(1)
    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(null)
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@HistorialViewModelTest.usuarioActual
        coEvery { sincronizar() } returns true
    }

    private fun TestScope.crearViewModel() = HistorialViewModel(repository).also { viewModel ->
        backgroundScope.launch { viewModel.pagos.collect {} }
        backgroundScope.launch { viewModel.empresas.collect {} }
    }

    @Test
    fun filtraPagosPorPeriodoYEmpresa() = runTest(regla.dispatcher) {
        every { repository.historialDeUsuario(1) } returns flowOf(
            listOf(
                DatosPrueba.pago(id = 1, organizacion = "Plaza Oriente", fechaPago = ahora - 10 * dia),
                DatosPrueba.pago(id = 2, organizacion = "Autopista Central", fechaPago = ahora - 60 * dia),
                DatosPrueba.pago(id = 3, organizacion = "Plaza Oriente", fechaPago = ahora - 200 * dia)
            )
        )
        usuarioActual.value = DatosPrueba.usuario(id = 1)
        val viewModel = crearViewModel()

        assertEquals(PeriodoHistorial.TRES_MESES, viewModel.periodo.value)
        assertEquals(listOf(1, 2), viewModel.pagos.value.map { it.id })
        assertEquals(listOf("Autopista Central", "Plaza Oriente"), viewModel.empresas.value)

        viewModel.onPeriodoChange(PeriodoHistorial.ULTIMO_MES)
        assertEquals(listOf(1), viewModel.pagos.value.map { it.id })

        viewModel.onPeriodoChange(PeriodoHistorial.DOCE_MESES)
        viewModel.onEmpresaChange("Plaza Oriente")
        assertEquals("Plaza Oriente", viewModel.empresa.value)
        assertEquals(listOf(1, 3), viewModel.pagos.value.map { it.id })

        viewModel.onEmpresaChange(null)
        assertNull(viewModel.empresa.value)
        assertEquals(3, viewModel.pagos.value.size)
    }

    @Test
    fun sinSesion_historialVacio() = runTest(regla.dispatcher) {
        val viewModel = crearViewModel()

        assertTrue(viewModel.pagos.value.isEmpty())
        assertTrue(viewModel.empresas.value.isEmpty())
    }
}
