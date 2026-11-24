package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.ReglaCorrutinas
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.remote.PagoFlexApi
import com.example.pagoflex.data.remote.RetrofitClient
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PagoFlexViewModelFactoryTest {

    @get:Rule
    val regla = ReglaCorrutinas()

    private val fabrica = PagoFlexViewModelFactory(
        repository = mockk<PagoFlexRepository>(relaxed = true) {
            every { usuarioActual } returns MutableStateFlow(null)
        },
        notificaciones = mockk<GestorNotificaciones>(relaxed = true),
        almacenImagenes = mockk<AlmacenImagenes>(relaxed = true)
    )

    @Before
    fun preparar() {
        mockkObject(RetrofitClient)
        every { RetrofitClient.indicadoresApi } returns mockk<PagoFlexApi>(relaxed = true)
    }

    @After
    fun limpiar() {
        unmockkObject(RetrofitClient)
    }

    @Test
    fun create_entregaCadaViewModelSoportado() {
        val clases = listOf(
            HomeViewModel::class.java,
            FormularioViewModel::class.java,
            CompromisosViewModel::class.java,
            HistorialViewModel::class.java,
            IncidenciasViewModel::class.java,
            PerfilViewModel::class.java,
            LoginViewModel::class.java,
            RegistroViewModel::class.java,
            RecuperarClaveViewModel::class.java,
            CambiarClaveViewModel::class.java,
            ConfirmarPagoViewModel::class.java,
            NuevoCompromisoViewModel::class.java
        )

        clases.forEach { clase -> assertTrue(clase.isInstance(fabrica.create(clase))) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun create_viewModelNoSoportado_lanzaExcepcion() {
        fabrica.create(ViewModelDesconocido::class.java)
    }

    private class ViewModelDesconocido : ViewModel()
}
