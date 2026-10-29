package com.example.studydesk.util

import android.content.Context

// Guarda en el teléfono quién tiene la sesión iniciada, para que siga abierta al reiniciar la app
class SesionManager(context: Context) {

    private val preferencias = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardarSesion(nombre: String) {
        preferencias.edit().putString("nombre", nombre).apply()
    }

    // Devuelve el nombre guardado, o null si no hay sesión iniciada
    fun obtenerNombre(): String? {
        return preferencias.getString("nombre", null)
    }

    fun cerrarSesion() {
        preferencias.edit().remove("nombre").apply()
    }
}