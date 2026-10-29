package com.example.studydesk.util

import android.util.Patterns

object Validaciones {

    // Validar nombre
    fun validarNombre(nombre: String): String? {
        if (nombre.isBlank()) {
            return "El nombre es obligatorio"
        }

        if (nombre.trim().length < 3) {
            return "El nombre debe tener al menos 3 caracteres"
        }

        return null
    }

    // Validar correo
    fun validarCorreo(correo: String): String? {
        if (correo.isBlank()) {
            return "El correo es obligatorio"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
            return "Ingresa un correo válido"
        }

        return null
    }

    // Validar contraseña
    fun validarContrasena(contrasena: String): String? {
        if (contrasena.isBlank()) {
            return "La contraseña es obligatoria"
        }

        if (contrasena.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres"
        }

        return null
    }
}