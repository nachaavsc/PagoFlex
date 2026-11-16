package com.example.pagoflex.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pagoflex.MainActivity
import com.example.pagoflex.R
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.ui.util.formatearFecha
import com.example.pagoflex.ui.util.formatearMonto

class GestorNotificaciones(private val context: Context) {

    fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_MOVIMIENTOS,
            "Pagos y compromisos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de pagos confirmados y nuevos compromisos asignados"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    fun permisoConcedido(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun notificarPago(pago: PagoEntity) {
        mostrar(
            id = ID_PAGO + pago.compromisoId,
            titulo = "Pago confirmado",
            texto = "Pagaste ${formatearMonto(pago.montoPagado)} a ${pago.organizacion} " +
                "(${pago.concepto}). Comprobante ${pago.comprobante}."
        )
    }

    fun notificarCompromisoAsignado(compromiso: CompromisoEntity, nombreCliente: String) {
        mostrar(
            id = ID_COMPROMISO + compromiso.folio.hashCode(),
            titulo = "Nuevo compromiso ${compromiso.folio}",
            texto = "Asignaste ${formatearMonto(compromiso.monto)} a $nombreCliente por " +
                "${compromiso.concepto}. Vence el ${formatearFecha(compromiso.fechaVencimiento)}."
        )
    }

    private fun mostrar(id: Int, titulo: String, texto: String) {
        if (!permisoConcedido()) return
        val abrirApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_MOVIMIENTOS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (e: SecurityException) {
            return
        }
    }

    private companion object {
        const val CANAL_MOVIMIENTOS = "pagoflex_movimientos"
        const val ID_PAGO = 1_000
        const val ID_COMPROMISO = 2_000
    }
}
