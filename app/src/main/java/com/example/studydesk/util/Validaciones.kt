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

    // Validar materia de un apunte
    fun validarMateriaApunte(materia: String): String? {
        if (materia.isBlank()) {
            return "La materia es obligatoria"
        }
        return null
    }

    // Validar título de un apunte
    fun validarTituloApunte(titulo: String): String? {
        if (titulo.isBlank()) {
            return "El título es obligatorio"
        }
        return null
    }

    // Validar contenido de un apunte
    fun validarContenidoApunte(contenido: String): String? {
        if (contenido.isBlank()) {
            return "El contenido es obligatorio"
        }
        return null
    }

    // Validar materia de una entrega
    fun validarMateriaEntrega(materia: String): String? {
        if (materia.isBlank()) {
            return "La materia es obligatoria"
        }
        return null
    }

    // Validar nombre del trabajo
    fun validarTituloEntrega(titulo: String): String? {
        if (titulo.isBlank()) {
            return "El nombre del trabajo es obligatorio"
        }
        return null
    }

    // Validar fecha de entrega
    fun validarFechaEntrega(fecha: String): String? {
        if (fecha.isBlank()) {
            return "Debes seleccionar una fecha de entrega"
        }
        return null
    }
}