
package com.example.studydesk.util

import android.content.Context
import com.example.studydesk.model.Usuario

class SesionManager(context: Context) {

    private val preferencias = context.getSharedPreferences(
        "sesion",
        Context.MODE_PRIVATE
    )

    // Guardar nombre e ID del usuario
    fun guardarSesion(usuario: Usuario) {
        preferencias.edit()
            .putInt("usuario_id", usuario.id)
            .putString("nombre", usuario.nombre)
            .apply()
    }

    // Mantener compatibilidad con el código actual
    fun guardarSesion(nombre: String) {
        preferencias.edit()
            .remove("usuario_id")
            .putString("nombre", nombre)
            .apply()
    }

    // Obtener el ID del usuario conectado
    fun obtenerIdUsuario(): Int? {
        val id = preferencias.getInt("usuario_id", -1)
        return if (id > 0) id else null
    }

    // Obtener el nombre del usuario conectado
    fun obtenerNombre(): String? {
        return preferencias.getString("nombre", null)
    }

    // Cerrar sesión sin borrar los datos de Room
    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }
}
