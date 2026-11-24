package com.example.pagoflex.viewmodel

import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.RegistroUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class RegistroViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val repository = mockk<PagoFlexRepository>()
    private val viewModel by lazy { RegistroViewModel(repository) }

    private fun completarFormulario() {
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onCorreoChange("ana@pagoflex.cl")
        viewModel.onTelefonoChange("+56 9 1234 5678")
        viewModel.onClaveChange(DatosPrueba.CLAVE_VALIDA)
        viewModel.onConfirmarClaveChange(DatosPrueba.CLAVE_VALIDA)
    }

    @Test
    fun crearCuenta_conDatosInvalidos_marcaCadaCampoYNoRegistra() {
        viewModel.onNombreChange("An")
        viewModel.onCorreoChange("ana@")
        viewModel.onTelefonoChange("123")
        viewModel.onClaveChange("clave")
        viewModel.onConfirmarClaveChange("otra")

        viewModel.crearCuenta()

        val estado = viewModel.uiState.value
        assertEquals("El nombre debe tener al menos 3 caracteres", estado.nombreError)
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", estado.correoError)
        assertEquals("Ingresa un teléfono válido, por ejemplo +56912345678", estado.telefonoError)
        assertEquals("La clave debe tener al menos 8 caracteres", estado.claveError)
        assertEquals("Las claves no coinciden", estado.confirmarClaveError)
        coVerify(exactly = 0) { repository.registrarUsuario(any(), any(), any(), any()) }
    }

    @Test
    fun crearCuenta_exitosa_registraEIniciaSesionConRolCliente() {
        coEvery { repository.registrarUsuario(any(), any(), any(), any()) } returns 7L
        coEvery { repository.iniciarSesion("ana@pagoflex.cl", DatosPrueba.CLAVE_VALIDA) } returns
            DatosPrueba.usuario(id = 7)
        completarFormulario()

        viewModel.crearCuenta()

        val estado = viewModel.uiState.value
        assertEquals(RolUsuario.CLIENTE, estado.rolAutenticado)
        assertFalse(estado.cargando)
        coVerify { repository.registrarUsuario("Ana Pérez", "ana@pagoflex.cl", "+56 9 1234 5678", DatosPrueba.CLAVE_VALIDA) }
    }

    @Test
    fun crearCuenta_conErrorDelServidor_muestraMensajeEnCorreo() {
        coEvery { repository.registrarUsuario(any(), any(), any(), any()) } throws
            IllegalStateException("El correo ya está registrado")
        completarFormulario()

        viewModel.crearCuenta()

        val estado = viewModel.uiState.value
        assertEquals("El correo ya está registrado", estado.correoError)
        assertNull(estado.rolAutenticado)
        assertFalse(estado.cargando)
        coVerify(exactly = 0) { repository.iniciarSesion(any(), any()) }
    }

    @Test
    fun cambiosDeCampo_corrigenErroresYNavegacionReiniciaEstado() {
        viewModel.crearCuenta()
        completarFormulario()

        val estado = viewModel.uiState.value
        assertNull(estado.nombreError)
        assertNull(estado.correoError)
        assertNull(estado.telefonoError)
        assertNull(estado.claveError)
        assertNull(estado.confirmarClaveError)

        viewModel.onNavegacionCompletada()
        assertEquals(RegistroUIState(), viewModel.uiState.value)
    }
}
