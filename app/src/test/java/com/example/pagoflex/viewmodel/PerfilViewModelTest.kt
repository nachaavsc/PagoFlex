package com.example.pagoflex.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.example.pagoflex.DatosPrueba
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.PerfilUIState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class PerfilViewModelTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val usuario = DatosPrueba.usuario(id = 1)
    private val usuarioActual = MutableStateFlow<UsuarioEntity?>(usuario)
    private val foto = mockk<Bitmap>()
    private val repository = mockk<PagoFlexRepository> {
        every { usuarioActual } returns this@PerfilViewModelTest.usuarioActual
    }
    private val almacenImagenes = mockk<AlmacenImagenes> {
        every { versionFotos } returns MutableStateFlow(0)
        coEvery { cargarFotoPerfil(1) } returns foto
    }

    @Test
    fun init_cargaDatosDelUsuarioYSuFoto() {
        val viewModel = PerfilViewModel(repository, almacenImagenes)

        assertEquals(
            PerfilUIState(nombre = "Ana Pérez", correo = "ana1@pagoflex.cl", telefono = "+56912345678", direccion = "Av. Siempre Viva 123"),
            viewModel.uiState.value
        )
        assertSame(foto, viewModel.fotoPerfil.value)
    }

    @Test
    fun guardarPerfil_conDatosInvalidosOCorreoDuplicado_noActualiza() {
        val viewModel = PerfilViewModel(repository, almacenImagenes)
        viewModel.onNombreChange("")
        viewModel.onCorreoChange("x")
        viewModel.onTelefonoChange("12")

        viewModel.guardarPerfil()

        assertEquals("El nombre es obligatorio", viewModel.uiState.value.nombreError)
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", viewModel.uiState.value.correoError)
        assertEquals("Ingresa un teléfono válido, por ejemplo +56912345678", viewModel.uiState.value.telefonoError)

        coEvery { repository.existeCorreo("luis@pagoflex.cl", 1) } returns true
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onCorreoChange("luis@pagoflex.cl")
        viewModel.onTelefonoChange("+56987654321")
        viewModel.guardarPerfil()

        assertEquals("Ya existe una cuenta con este correo", viewModel.uiState.value.correoError)
        assertFalse(viewModel.uiState.value.cargando)
        coVerify(exactly = 0) { repository.actualizarPerfil(any(), any(), any(), any()) }
    }

    @Test
    fun guardarPerfil_exitosoYConErrorDeServidor() {
        coEvery { repository.existeCorreo(any(), any()) } returns false
        coEvery { repository.actualizarPerfil("Ana Pérez", "ana1@pagoflex.cl", "+56912345678", "Los Leones 45") } returns
            Unit andThenThrows IllegalStateException("Error del servidor (500)")
        val viewModel = PerfilViewModel(repository, almacenImagenes)
        viewModel.onDireccionChange("Los Leones 45")

        viewModel.guardarPerfil()
        assertEquals("Perfil actualizado correctamente", viewModel.uiState.value.mensajeExito)

        viewModel.onMensajeMostrado()
        assertNull(viewModel.uiState.value.mensajeExito)

        viewModel.guardarPerfil()
        assertEquals("Error del servidor (500)", viewModel.uiState.value.correoError)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun fotos_informanResultadoDeGuardadoYErroresDeCamara() = runTest(regla.dispatcher) {
        val tomada = mockk<Bitmap>()
        val uri = mockk<Uri>()
        coEvery { almacenImagenes.guardarFotoPerfil(1, tomada) } returns tomada
        coEvery { almacenImagenes.guardarFotoPerfil(1, uri) } returns null
        val viewModel = PerfilViewModel(repository, almacenImagenes)

        viewModel.onFotoTomada(tomada)
        assertEquals(EventoUI.Exito("Foto de perfil actualizada"), viewModel.eventos.first())

        viewModel.onImagenSeleccionada(uri)
        assertEquals(EventoUI.Error("No se pudo guardar la imagen seleccionada"), viewModel.eventos.first())

        viewModel.onErrorFoto("Permiso de cámara denegado")
        assertEquals(EventoUI.Error("Permiso de cámara denegado"), viewModel.eventos.first())
    }

    @Test
    fun cerrarSesion_sinUsuarioNoGuardaFotos() {
        every { repository.cerrarSesion() } just runs
        val viewModel = PerfilViewModel(repository, almacenImagenes)

        viewModel.cerrarSesion()
        usuarioActual.value = null
        viewModel.onFotoTomada(mockk())

        verify { repository.cerrarSesion() }
        coVerify(exactly = 0) { almacenImagenes.guardarFotoPerfil(any(), any<Bitmap>()) }
    }
}
