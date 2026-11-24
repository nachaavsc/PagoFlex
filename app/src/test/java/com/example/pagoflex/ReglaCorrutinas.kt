package com.example.pagoflex

import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ReglaCorrutinas(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

object DatosPrueba {

    const val CLAVE_VALIDA = "Clave#2024"

    fun usuario(
        id: Int = 1,
        nombre: String = "Ana Pérez",
        rol: String = RolUsuario.CLIENTE,
        clave: String = ""
    ) = UsuarioEntity(
        id = id,
        nombre = nombre,
        correo = "ana$id@pagoflex.cl",
        clave = clave,
        rol = rol,
        telefono = "+56912345678",
        direccion = "Av. Siempre Viva 123",
        fechaRegistro = 0L
    )

    fun compromiso(
        id: Int = 1,
        usuarioId: Int = 1,
        estado: String = EstadoPago.PENDIENTE,
        monto: Long = 10_000L,
        fechaVencimiento: Long = 1_000_000L
    ) = CompromisoEntity(
        id = id,
        usuarioId = usuarioId,
        folio = "PF-%05d".format(id),
        organizacion = "Plaza Oriente",
        concepto = "Arriendo",
        cuotaActual = 1,
        totalCuotas = 1,
        monto = monto,
        fechaVencimiento = fechaVencimiento,
        estado = estado
    )

    fun pago(
        id: Int = 1,
        compromisoId: Int = 1,
        organizacion: String = "Plaza Oriente",
        fechaPago: Long = System.currentTimeMillis()
    ) = PagoEntity(
        id = id,
        compromisoId = compromisoId,
        montoPagado = 10_000L,
        fechaPago = fechaPago,
        folio = "PF-%05d".format(compromisoId),
        organizacion = organizacion,
        concepto = "Arriendo",
        cuotaActual = 1,
        totalCuotas = 1,
        canal = "App Móvil PagoFlex",
        comprobante = "PF-PAGO-$id"
    )

    fun incidencia(
        id: Int = 1,
        usuarioId: Int = 1,
        estado: String = EstadoIncidencia.EN_REVISION
    ) = IncidenciaEntity(
        id = id,
        usuarioId = usuarioId,
        folio = "PF-INC-%05d".format(id),
        motivo = "Cobro duplicado",
        detalle = "Se cobró dos veces la misma cuota",
        fechaReporte = 0L,
        estado = estado,
        comprobante = "PF-INC-API-$id"
    )
}
