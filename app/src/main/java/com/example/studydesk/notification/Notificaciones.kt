package com.example.studydesk.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notificaciones {

    private const val ID_CANAL = "cambios_normativos"
    private const val ID_NOTIFICACION = 1

    // Android 8 o superior exige un canal para poder mostrar notificaciones.
    // En versiones anteriores no existe el concepto de canal, así que no se hace nada.
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL,
                "Cambios normativos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de modificaciones en la legislación"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    // En Android 13 o superior hay que tener el permiso concedido por el usuario
    fun tienePermiso(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    // El permiso se revisa con tienePermiso() antes de mostrar la notificación
    @SuppressLint("MissingPermission")
    fun mostrarAvisoCambio(context: Context, titulo: String, mensaje: String) {
        if (!tienePermiso(context)) return

        val notificacion = NotificationCompat.Builder(context, ID_CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_NOTIFICACION, notificacion)
    }
}