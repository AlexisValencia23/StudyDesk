package com.example.studydesk.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Usuario
import com.example.studydesk.repository.UsuarioRepository
import kotlinx.coroutines.launch

class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {

    // Mensaje que la pantalla muestra cuando algo sale mal (null = sin error)
    var mensajeError by mutableStateOf<String?>(null)
        private set

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        perfil: String,
        onExito: (String) -> Unit
    ) {
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
                mensajeError = null
                onExito(nombre.trim())
            }
        }
    }

    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onExito: (String) -> Unit
    ) {
        viewModelScope.launch {
            val usuario = repository.buscarPorCorreo(correo.trim().lowercase())

            if (usuario != null && usuario.contrasena == contrasena) {
                mensajeError = null
                onExito(usuario.nombre)
            } else {
                mensajeError = "Correo o contraseña incorrectos"
            }
        }
    }

    fun limpiarError() {
        mensajeError = null
    }
}