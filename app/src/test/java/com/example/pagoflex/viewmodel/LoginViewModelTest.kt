package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.LoginUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val repository = mockk<PagoFlexRepository>()
    private val viewModel by lazy { LoginViewModel(repository) }

    @Test
    fun iniciarSesion_conCamposVacios_muestraErroresSinConsultarRepositorio() {
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals("El correo es obligatorio", estado.correoError)
        assertEquals("La clave es obligatoria", estado.claveError)
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    @Test
    fun iniciarSesion_conCredencialesValidas_asignaRolYLimpiaClave() {
        coEvery { repository.iniciarSesion("ana@pagoflex.cl", "Clave#2024") } returns
            DatosPrueba.usuario(rol = RolUsuario.GESTOR)

        viewModel.onCorreoChange("ana@pagoflex.cl")
        viewModel.onClaveChange("Clave#2024")
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals(RolUsuario.GESTOR, estado.rolAutenticado)
        assertEquals("", estado.clave)
        assertFalse(estado.cargando)
        assertNull(estado.errorGeneral)
    }

    @Test
    fun iniciarSesion_conCredencialesIncorrectas_muestraErrorGeneral() {
        coEvery { repository.iniciarSesion(any(), any()) } returns null

        viewModel.onCorreoChange("ana@pagoflex.cl")
        viewModel.onClaveChange("incorrecta")
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals("Correo o clave incorrectos", estado.errorGeneral)
        assertNull(estado.rolAutenticado)
        assertFalse(estado.cargando)
    }

    @Test
    fun cambiosDeCampo_revalidanErroresYNavegacionReiniciaEstado() {
        viewModel.iniciarSesion()
        assertNotNull(viewModel.uiState.value.correoError)

        viewModel.onCorreoChange("correo-invalido")
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", viewModel.uiState.value.correoError)

        viewModel.onCorreoChange("ana@pagoflex.cl")
        viewModel.onClaveChange("x")
        assertNull(viewModel.uiState.value.correoError)
        assertNull(viewModel.uiState.value.claveError)

        viewModel.onNavegacionCompletada()
        assertEquals(LoginUIState(), viewModel.uiState.value)
    }
}
