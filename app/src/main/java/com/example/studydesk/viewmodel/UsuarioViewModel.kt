
package com.example.studydesk.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Usuario
import com.example.studydesk.repository.UsuarioRepository
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.launch

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Registrar un usuario
    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        perfil: String,
        onExito: (Usuario) -> Unit
    ) {
        mensajeError = Validaciones.validarNombre(nombre)
            ?: Validaciones.validarCorreo(correo)
                    ?: Validaciones.validarContrasena(contrasena)

        if (mensajeError != null) return

        viewModelScope.launch {
            val correoLimpio = correo.trim().lowercase()

            if (repository.buscarPorCorreo(correoLimpio) != null) {
                mensajeError = "Ya existe una cuenta con ese correo"
            } else {

                repository.crearUsuario(
                    Usuario(
                        nombre = nombre.trim(),
                        correo = correoLimpio,
                        contrasena = contrasena,
                        perfil = perfil
                    )
                )

                // Recuperar el usuario con su ID generado por Room
                val usuarioGuardado =
                    repository.buscarPorCorreo(correoLimpio)

                if (usuarioGuardado != null) {
                    mensajeError = null
                    onExito(usuarioGuardado)
                } else {
                    mensajeError = "No se pudo recuperar la cuenta creada"
                }
            }
        }
    }

    // Iniciar sesión
    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onExito: (Usuario) -> Unit
    ) {
        mensajeError = Validaciones.validarCorreo(correo)
            ?: Validaciones.validarContrasena(contrasena)

        if (mensajeError != null) return

        viewModelScope.launch {
            val usuario = repository.buscarPorCorreo(
                correo.trim().lowercase()
            )

            if (
                usuario != null &&
                usuario.contrasena == contrasena
            ) {
                mensajeError = null

                // Enviar el usuario completo, incluido su ID
                onExito(usuario)
            } else {
                mensajeError = "Correo o contraseña incorrectos"
            }
        }
    }

    fun limpiarError() {
        mensajeError = null
    }
}
