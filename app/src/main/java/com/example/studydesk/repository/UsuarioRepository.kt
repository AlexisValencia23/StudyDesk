package com.example.studydesk.repository

import com.example.studydesk.model.Usuario
import com.example.studydesk.model.UsuarioDao

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun crearUsuario(usuario: Usuario) {
        usuarioDao.insertar(usuario)
    }

    suspend fun buscarPorCorreo(correo: String): Usuario? {
        return usuarioDao.buscarPorCorreo(correo)
    }
}