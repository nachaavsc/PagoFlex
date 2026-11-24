package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.RecuperarClaveUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class RecuperarClaveViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val repository = mockk<PagoFlexRepository>()
    private val viewModel by lazy { RecuperarClaveViewModel(repository) }

    private fun completar() {
        viewModel.onCorreoChange("ana@pagoflex.cl")
        viewModel.onTelefonoChange("+56912345678")
        viewModel.onClaveChange(DatosPrueba.CLAVE_VALIDA)
        viewModel.onConfirmarClaveChange(DatosPrueba.CLAVE_VALIDA)
    }

    @Test
    fun restablecer_conCamposInvalidos_marcaErroresYLuegoLosCorrige() {
        viewModel.onClaveChange("sinmayuscula1!")
        viewModel.restablecerClave()

        val estado = viewModel.uiState.value
        assertEquals("El correo es obligatorio", estado.correoError)
        assertEquals("El teléfono es obligatorio", estado.telefonoError)
        assertEquals("La clave debe incluir al menos una mayúscula", estado.claveError)
        assertEquals("Confirma tu clave", estado.confirmarClaveError)
        coVerify(exactly = 0) { repository.restablecerClave(any(), any(), any()) }

        completar()
        val corregido = viewModel.uiState.value
        assertNull(corregido.correoError)
        assertNull(corregido.telefonoError)
        assertNull(corregido.claveError)
        assertNull(corregido.confirmarClaveError)
    }

    @Test
    fun restablecer_exitoso_muestraMensajeYLuegoReinicia() {
        coEvery { repository.restablecerClave("ana@pagoflex.cl", "+56912345678", DatosPrueba.CLAVE_VALIDA) } returns true
        completar()

        viewModel.restablecerClave()

        assertEquals("Clave actualizada. Ya puedes iniciar sesión.", viewModel.uiState.value.mensajeExito)
        assertFalse(viewModel.uiState.value.cargando)
        viewModel.onMensajeMostrado()
        assertEquals(RecuperarClaveUIState(), viewModel.uiState.value)
    }

    @Test
    fun restablecer_sinCoincidenciaOConErrorDeRed_muestraErrorGeneral() {
        coEvery { repository.restablecerClave(any(), any(), any()) } returns false andThenThrows IllegalStateException("Sin red")
        completar()

        viewModel.restablecerClave()
        assertEquals("No encontramos una cuenta con ese correo y teléfono", viewModel.uiState.value.errorGeneral)

        viewModel.restablecerClave()
        assertEquals("No encontramos una cuenta con ese correo y teléfono", viewModel.uiState.value.errorGeneral)
        assertFalse(viewModel.uiState.value.cargando)
    }
}
