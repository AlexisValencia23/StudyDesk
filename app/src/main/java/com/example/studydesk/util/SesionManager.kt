
package com.example.studydesk.util

import android.content.Context
import com.example.studydesk.model.Usuario
import java.util.Locale

class SesionManager(context: Context) {

    private val preferencias = context.getSharedPreferences(
        "sesion",
        Context.MODE_PRIVATE
    )

    // Guardar los datos del usuario conectado
    fun guardarSesion(usuario: Usuario) {
        preferencias.edit()
            .putInt("usuario_id", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString(
                "correo",
                usuario.correo.trim().lowercase(Locale.ROOT)
            )
            .apply()
    }

    // Mantener compatibilidad con el codigo anterior
    fun guardarSesion(nombre: String) {
        preferencias.edit()
            .remove("usuario_id")
            .remove("correo")
            .putString("nombre", nombre)
            .apply()
    }

    // Obtener ID local de Room
    fun obtenerIdUsuario(): Int? {
        val id = preferencias.getInt("usuario_id", -1)
        return if (id > 0) id else null
    }

    // Obtener nombre del usuario
    fun obtenerNombre(): String? {
        return preferencias.getString("nombre", null)
    }

    // Obtener correo del usuario conectado
    fun obtenerCorreoUsuario(): String? {
        return preferencias.getString("correo", null)
    }

    // Cerrar sesion sin borrar los datos de Room
    fun cerrarSesion() {
        preferencias.edit()
            .clear()
            .apply()
    }
}
