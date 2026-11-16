package com.example.pagoflex.data.archivos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AlmacenImagenes(private val context: Context) {

    private val _versionFotos = MutableStateFlow(0)
    val versionFotos: StateFlow<Int> = _versionFotos.asStateFlow()

    private val carpeta: File
        get() = File(context.filesDir, CARPETA_FOTOS).apply { mkdirs() }

    suspend fun cargarFotoPerfil(usuarioId: Int): Bitmap? = withContext(Dispatchers.IO) {
        val archivo = archivoPerfil(usuarioId)
        if (archivo.exists()) BitmapFactory.decodeFile(archivo.absolutePath) else null
    }

    suspend fun guardarFotoPerfil(usuarioId: Int, bitmap: Bitmap): Bitmap? = withContext(Dispatchers.IO) {
        escribir(archivoPerfil(usuarioId), escalar(bitmap))
    }

    suspend fun guardarFotoPerfil(usuarioId: Int, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        decodificar(uri)?.let { escribir(archivoPerfil(usuarioId), escalar(it)) }
    }

    private fun archivoPerfil(usuarioId: Int) = File(carpeta, "perfil_$usuarioId.jpg")

    private fun decodificar(uri: Uri): Bitmap? = try {
        val resolver = context.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            null
        } else {
            var muestreo = 1
            while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        }
    } catch (e: IOException) {
        null
    } catch (e: SecurityException) {
        null
    }

    private fun escalar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val factor = LADO_MAXIMO.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * factor).toInt(),
            (bitmap.height * factor).toInt(),
            true
        )
    }

    private fun escribir(archivo: File, bitmap: Bitmap): Bitmap? = try {
        FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, it) }
        _versionFotos.update { it + 1 }
        bitmap
    } catch (e: IOException) {
        null
    }

    private companion object {
        const val CARPETA_FOTOS = "fotos"
        const val LADO_MAXIMO = 720
        const val CALIDAD_JPEG = 85
    }
}
