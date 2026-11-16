package com.example.pagoflex.data.seguridad

import java.security.MessageDigest

object Hash {

    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
