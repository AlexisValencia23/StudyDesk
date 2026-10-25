package com.example.studydesk.viewmodel

// Reglas de validación de los formularios de usuario.
// Cada función devuelve el mensaje de error, o null si el valor es válido.
object ValidadorUsuario {

    private val formatoCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    private const val LARGO_MINIMO_NOMBRE = 3
    private const val LARGO_MINIMO_CONTRASENA = 8

    fun validarNombre(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre es obligatorio"
        nombre.trim().length < LARGO_MINIMO_NOMBRE -> "Ingresa al menos $LARGO_MINIMO_NOMBRE caracteres"
        else -> null
    }

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !formatoCorreo.matches(correo.trim()) -> "Ingresa un correo válido (nombre@correo.cl)"
        else -> null
    }

    // Para crear cuenta: obligatoria y con largo mínimo
    fun validarContrasena(contrasena: String): String? = when {
        contrasena.isBlank() -> "La contraseña es obligatoria"
        contrasena.length < LARGO_MINIMO_CONTRASENA -> "Mínimo $LARGO_MINIMO_CONTRASENA caracteres"
        else -> null
    }

    // Para iniciar sesión: solo se exige que no esté vacía
    fun validarContrasenaObligatoria(contrasena: String): String? =
        if (contrasena.isBlank()) "La contraseña es obligatoria" else null
}