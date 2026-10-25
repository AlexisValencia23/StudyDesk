package com.example.studydesk.viewmodel

// Campos que pueden tener un error de validación
enum class CampoUsuario { NOMBRE, CORREO, CONTRASENA }

// Un mensaje por campo (null = el campo no tiene error)
data class ErroresFormulario(
    val nombre: String? = null,
    val correo: String? = null,
    val contrasena: String? = null
) {
    fun hayErrores(): Boolean = nombre != null || correo != null || contrasena != null
}