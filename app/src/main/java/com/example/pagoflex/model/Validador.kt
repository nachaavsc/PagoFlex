package com.example.pagoflex.model

object Validador {

    private val regexCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val regexTelefono = Regex("^\\+?[0-9]{8,12}$")
    private val regexEspecial = Regex("[^A-Za-z0-9]")

    const val MONTO_MAXIMO = 10_000_000L

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
        else -> null
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !regexCorreo.matches(valor.trim()) -> "Ingresa un correo válido, por ejemplo nombre@dominio.cl"
        else -> null
    }

    fun telefono(valor: String): String? = when {
        valor.isBlank() -> "El teléfono es obligatorio"
        !regexTelefono.matches(normalizarTelefono(valor)) -> "Ingresa un teléfono válido, por ejemplo +56912345678"
        else -> null
    }

    fun monto(valor: String): String? {
        if (valor.isBlank()) return "El monto es obligatorio"
        val numero = valor.trim().toLongOrNull() ?: return "Ingresa solo números, sin puntos ni signos"
        return when {
            numero <= 0 -> "El monto debe ser mayor a 0"
            numero > MONTO_MAXIMO -> "El monto no puede superar \$10.000.000"
            else -> null
        }
    }

    fun clave(valor: String): String? = when {
        valor.isEmpty() -> "La clave es obligatoria"
        valor.length < 8 -> "La clave debe tener al menos 8 caracteres"
        valor.none { it.isUpperCase() } -> "La clave debe incluir al menos una mayúscula"
        valor.none { it.isDigit() } -> "La clave debe incluir al menos un número"
        !regexEspecial.containsMatchIn(valor) -> "La clave debe incluir al menos un carácter especial"
        else -> null
    }

    fun confirmarClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Confirma tu clave"
        confirmacion != clave -> "Las claves no coinciden"
        else -> null
    }

    fun obligatorio(valor: String, mensaje: String): String? = if (valor.isBlank()) mensaje else null

    fun revalidar(errorActual: String?, validacion: () -> String?): String? =
        if (errorActual != null) validacion() else null

    fun normalizarTelefono(valor: String): String = valor.filterNot { it.isWhitespace() }
}
