package com.example.pagoflex.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class Vibrador(context: Context) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun toque() = vibrar(DURACION_TOQUE_MS)

    fun confirmacion() = vibrar(DURACION_CONFIRMACION_MS)

    private fun vibrar(duracionMs: Long) {
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(duracionMs)
        }
    }

    private companion object {
        const val DURACION_TOQUE_MS = 20L
        const val DURACION_CONFIRMACION_MS = 60L
    }
}

@Composable
fun rememberVibrador(): Vibrador {
    val context = LocalContext.current
    return remember(context) { Vibrador(context.applicationContext) }
}
