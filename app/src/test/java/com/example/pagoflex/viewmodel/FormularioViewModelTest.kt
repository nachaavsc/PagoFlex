package com.example.pagoflex.viewmodel

import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.FormularioUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FormularioViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val repository = mockk<PagoFlexRepository> {
        every { verificarClave("Clave#2024") } returns true
        every { verificarClave("otra") } returns false
    }
    private val viewModel by lazy { FormularioViewModel(repository) }

    @Test
    fun guardar_conClaveIncorrectaYCamposVacios_noRegistraTransaccion() {
        viewModel.onClaveChange("otra")
        viewModel.guardarTransaccion()

        val estado = viewModel.uiState.value
        assertEquals("El nombre es obligatorio", estado.nombreError)
        assertEquals("El correo es obligatorio", estado.correoError)
        assertEquals("El monto es obligatorio", estado.montoError)
        assertEquals("La clave no coincide con tu cuenta", estado.claveError)
        coVerify(exactly = 0) { repository.agregarTransaccion(any()) }
    }

    @Test
    fun guardar_valido_registraTransaccionYPermiteReiniciar() {
        val transaccion = slot<TransaccionEntity>()
        coEvery { repository.agregarTransaccion(capture(transaccion)) } just runs
        viewModel.guardarTransaccion()
        viewModel.onNombreChange(" Plaza Oriente ")
        viewModel.onCorreoChange("pagos@plaza.cl")
        viewModel.onMontoChange("25000")
        viewModel.onClaveChange("Clave#2024")
        assertNull(viewModel.uiState.value.claveError)

        viewModel.guardarTransaccion()

        assertTrue(viewModel.uiState.value.guardadoExitoso)
        assertEquals("Pago a Plaza Oriente", transaccion.captured.concepto)
        assertEquals(25000.0, transaccion.captured.monto, 0.0)
        viewModel.resetGuardado()
        assertEquals(FormularioUIState(), viewModel.uiState.value)
    }
}
