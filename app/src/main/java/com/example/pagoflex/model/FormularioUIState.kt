package com.example.pagoflex.model

data class FormularioUIState(
    val nombre: String = "",
    val nombreError: String? = null,
    val correo: String = "",
    val correoError: String? = null,
    val monto: String = "",
    val montoError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false
)

data class LoginUIState(
    val correo: String = "",
    val correoError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val rolAutenticado: String? = null
)

data class RegistroUIState(
    val nombre: String = "",
    val nombreError: String? = null,
    val correo: String = "",
    val correoError: String? = null,
    val telefono: String = "",
    val telefonoError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val confirmarClave: String = "",
    val confirmarClaveError: String? = null,
    val cargando: Boolean = false,
    val rolAutenticado: String? = null
)

data class RecuperarClaveUIState(
    val correo: String = "",
    val correoError: String? = null,
    val telefono: String = "",
    val telefonoError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val confirmarClave: String = "",
    val confirmarClaveError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val mensajeExito: String? = null
)

data class PerfilUIState(
    val nombre: String = "",
    val nombreError: String? = null,
    val correo: String = "",
    val correoError: String? = null,
    val telefono: String = "",
    val telefonoError: String? = null,
    val direccion: String = "",
    val cargando: Boolean = false,
    val mensajeExito: String? = null
)

data class IncidenciaUIState(
    val motivo: String = "",
    val motivoError: String? = null,
    val detalle: String = "",
    val detalleError: String? = null,
    val cargando: Boolean = false
)

data class NuevoCompromisoUIState(
    val clienteId: Int? = null,
    val clienteNombre: String = "",
    val clienteError: String? = null,
    val organizacion: String = "",
    val organizacionError: String? = null,
    val concepto: String = "",
    val conceptoError: String? = null,
    val monto: String = "",
    val montoError: String? = null,
    val fechaVencimiento: Long? = null,
    val fechaError: String? = null,
    val cargando: Boolean = false
)

data class CambiarClaveUIState(
    val claveActual: String = "",
    val claveActualError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val confirmarClave: String = "",
    val confirmarClaveError: String? = null,
    val cargando: Boolean = false,
    val mensajeExito: String? = null
)
