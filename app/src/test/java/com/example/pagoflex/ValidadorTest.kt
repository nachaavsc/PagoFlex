package com.example.pagoflex

import com.example.pagoflex.data.seguridad.Hash
import com.example.pagoflex.model.Validador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorTest {

    @Test
    fun correo_rechazaFormatosInvalidosYAceptaValidos() {
        assertEquals("El correo es obligatorio", Validador.correo(""))
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", Validador.correo("nombre@dominio"))
        assertEquals("Ingresa un correo válido, por ejemplo nombre@dominio.cl", Validador.correo("nombre.dominio.cl"))
        assertNull(Validador.correo("camila.rojas@pagoflex.cl"))
    }

    @Test
    fun monto_exigeRangoEntreUnoYDiezMillones() {
        assertEquals("El monto es obligatorio", Validador.monto(""))
        assertEquals("Ingresa solo números, sin puntos ni signos", Validador.monto("45.000"))
        assertEquals("El monto debe ser mayor a 0", Validador.monto("0"))
        assertEquals("El monto no puede superar \$10.000.000", Validador.monto("10000001"))
        assertNull(Validador.monto("1"))
        assertNull(Validador.monto("10000000"))
    }

    @Test
    fun clave_exigeLargoMayusculaNumeroYCaracterEspecial() {
        assertEquals("La clave es obligatoria", Validador.clave(""))
        assertEquals("La clave debe tener al menos 8 caracteres", Validador.clave("Ab1#"))
        assertEquals("La clave debe incluir al menos una mayúscula", Validador.clave("abcdef1#"))
        assertEquals("La clave debe incluir al menos un número", Validador.clave("Abcdefg#"))
        assertEquals("La clave debe incluir al menos un carácter especial", Validador.clave("Abcdefg1"))
        assertNull(Validador.clave("Cliente#2026"))
    }

    @Test
    fun confirmarClave_detectaDiferencias() {
        assertEquals("Confirma tu clave", Validador.confirmarClave("Cliente#2026", ""))
        assertEquals("Las claves no coinciden", Validador.confirmarClave("Cliente#2026", "Cliente#2025"))
        assertNull(Validador.confirmarClave("Cliente#2026", "Cliente#2026"))
    }

    @Test
    fun nombreYTelefono_validanContenidoMinimo() {
        assertEquals("El nombre es obligatorio", Validador.nombre("  "))
        assertEquals("El nombre debe tener al menos 3 caracteres", Validador.nombre("Al"))
        assertNull(Validador.nombre("Camila Rojas"))
        assertEquals("El teléfono es obligatorio", Validador.telefono(""))
        assertEquals("Ingresa un teléfono válido, por ejemplo +56912345678", Validador.telefono("12ab"))
        assertNull(Validador.telefono("+56 9 1234 5678"))
    }

    @Test
    fun hash_generaSha256HexadecimalDeterministico() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Hash.sha256("abc")
        )
        assertEquals(Hash.sha256("Cliente#2026"), Hash.sha256("Cliente#2026"))
        assertNotEquals("Cliente#2026", Hash.sha256("Cliente#2026"))
    }
}
