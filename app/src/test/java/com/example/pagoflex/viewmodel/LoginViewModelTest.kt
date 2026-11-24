package com.example.pagoflex.viewmodel

import android.graphics.Bitmap
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.DatosPrueba
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.data.repository.ReglaCorrutinas
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(DatosPrueba.usuario(id = 1))
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@LoginViewModelTest.usuarioActual
    }
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
    fun perfil_cargaDatosGuardaCambiosYManejaErrorDeServidor() {
        val foto = mockk<Bitmap>()
        val almacenImagenes = mockk<AlmacenImagenes> {
            every { versionFotos } returns MutableStateFlow(0)
            coEvery { cargarFotoPerfil(1) } returns foto
        }
        coEvery { repository.existeCorreo(any(), any()) } returns false
        coEvery { repository.actualizarPerfil("Ana Pérez", "ana1@pagoflex.cl", "+56912345678", "Los Leones 45") } returns
            Unit andThenThrows IllegalStateException("Error del servidor (500)")
        val perfil = PerfilViewModel(repository, almacenImagenes)

        assertEquals("Ana Pérez", perfil.uiState.value.nombre)
        assertSame(foto, perfil.fotoPerfil.value)

        perfil.onDireccionChange("Los Leones 45")
        perfil.guardarPerfil()
        assertEquals("Perfil actualizado correctamente", perfil.uiState.value.mensajeExito)

        perfil.onMensajeMostrado()
        perfil.guardarPerfil()
        assertNull(perfil.uiState.value.mensajeExito)
        assertEquals("Error del servidor (500)", perfil.uiState.value.correoError)
        assertFalse(perfil.uiState.value.cargando)
    }
}
