
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Comentario
import com.example.studydesk.repository.ComentarioRepository
import com.example.studydesk.util.FechaUtil
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ComentarioViewModel(
    private val repository: ComentarioRepository
) : ViewModel() {

    // Usuario que tiene la sesion iniciada
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    fun establecerUsuario(usuarioId: Int?) {
        usuarioActivo.value = usuarioId
    }

    // Obtener todos los comentarios de un material
    fun comentarios(
        archivoPdf: String
    ): Flow<List<Comentario>> {
        return repository.obtenerPorMaterial(archivoPdf)
    }

    // Crear comentario
    fun agregarComentario(
        archivoPdf: String,
        autor: String,
        texto: String
    ) {
        val usuarioId = usuarioActivo.value ?: return

        if (Validaciones.validarComentario(texto) != null) {
            return
        }

        viewModelScope.launch {
            repository.agregarComentario(
                Comentario(
                    archivoPdf = archivoPdf,
                    autor = autor,
                    autorId = usuarioId,
                    texto = texto.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }

    // Editar comentario propio
    fun editarComentario(
        comentario: Comentario,
        nuevoTexto: String
    ) {
        val usuarioId = usuarioActivo.value ?: return

        // Verificar que pertenece al usuario
        if (comentario.autorId != usuarioId) {
            return
        }

        if (Validaciones.validarComentario(nuevoTexto) != null) {
            return
        }

        viewModelScope.launch {
            repository.editarComentario(
                comentario.copy(
                    texto = nuevoTexto.trim(),
                    fecha = FechaUtil.hoy()
                ),
                usuarioId
            )
        }
    }

    // Eliminar comentario propio
    fun eliminarComentario(comentario: Comentario) {
        val usuarioId = usuarioActivo.value ?: return

        // Impedir eliminar comentarios ajenos
        if (comentario.autorId != usuarioId) {
            return
        }

        viewModelScope.launch {
            repository.eliminarComentario(
                comentarioId = comentario.id,
                usuarioId = usuarioId
            )
        }
    }
}
