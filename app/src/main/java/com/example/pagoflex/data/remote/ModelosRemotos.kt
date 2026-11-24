package com.example.pagoflex.data.remote

data class UsuarioRemoto(
    val id: Long? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val rutaFoto: String? = null,
    val rol: String? = null
)

data class CompromisoRemoto(
    val id: Long? = null,
    val usuarioId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val montoTotal: Double,
    val montoPagado: Double? = null,
    val cuotasTotales: Int? = null,
    val cuotasPagadas: Int? = null,
    val fechaVencimiento: String? = null,
    val estado: String? = null
)

data class PagoRemoto(
    val id: Long? = null,
    val compromisoId: Long,
    val monto: Double,
    val metodoPago: String? = null,
    val fechaPago: String? = null,
    val comprobante: String? = null
)

data class IncidenciaRemota(
    val id: Long? = null,
    val usuarioId: Long,
    val compromisoId: Long? = null,
    val titulo: String,
    val descripcion: String,
    val estado: String? = null,
    val respuestaSoporte: String? = null,
    val fechaCreacion: String? = null
)
