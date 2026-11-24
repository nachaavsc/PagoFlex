package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.CambiarClaveUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CambiarClaveViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val repository = mockk<PagoFlexRepository>()
    private val viewModel by lazy { CambiarClaveViewModel(repository) }

    private fun completar(actual: String = "Anterior#1", nueva: String = DatosPrueba.CLAVE_VALIDA) {
        viewModel.onClaveActualChange(actual)
        viewModel.onClaveChange(nueva)
        viewModel.onConfirmarClaveChange(nueva)
    }

    @Test
    fun cambiarClave_conCamposInvalidos_marcaErroresSinLlamarRepositorio() {
        viewModel.cambiarClave()

        assertEquals("Ingresa tu clave actual", viewModel.uiState.value.claveActualError)
        assertEquals("La clave es obligatoria", viewModel.uiState.value.claveError)
        assertEquals("Confirma tu clave", viewModel.uiState.value.confirmarClaveError)

        completar(actual = DatosPrueba.CLAVE_VALIDA)
        assertEquals("La nueva clave debe ser distinta a la actual", viewModel.uiState.value.claveError)
        assertNull(viewModel.uiState.value.claveActualError)
        assertNull(viewModel.uiState.value.confirmarClaveError)
        coVerify(exactly = 0) { repository.cambiarClave(any(), any()) }
    }

    @Test
    fun cambiarClave_exitoso_reiniciaFormularioConMensaje() {
        coEvery { repository.cambiarClave("Anterior#1", DatosPrueba.CLAVE_VALIDA) } returns true
        completar()

        viewModel.cambiarClave()

        assertEquals(CambiarClaveUIState(mensajeExito = "Clave actualizada correctamente"), viewModel.uiState.value)
        viewModel.onMensajeMostrado()
        assertNull(viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun cambiarClave_rechazadaOConErrorDeRed_indicaClaveActualIncorrecta() {
        coEvery { repository.cambiarClave(any(), any()) } returns false andThenThrows IllegalStateException("Sin conexión")
        completar()

        viewModel.cambiarClave()
        assertEquals("La clave actual no es correcta", viewModel.uiState.value.claveActualError)

        completar()
        viewModel.cambiarClave()
        assertEquals("La clave actual no es correcta", viewModel.uiState.value.claveActualError)
        assertFalse(viewModel.uiState.value.cargando)
    }
}
