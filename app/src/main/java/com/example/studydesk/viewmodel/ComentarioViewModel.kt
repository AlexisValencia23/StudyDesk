
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Comentario
import com.example.studydesk.repository.ComentarioRepository
import com.example.studydesk.util.FechaUtil
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ComentarioViewModel(
    private val repository: ComentarioRepository
) : ViewModel() {

    // Obtener los comentarios de cada material
    fun comentarios(archivoPdf: String): Flow<List<Comentario>> {
        return repository.obtenerPorMaterial(archivoPdf)
    }

    // Agregar comentario con validación
    fun agregarComentario(
        archivoPdf: String,
        autor: String,
        texto: String
    ) {
        if (Validaciones.validarComentario(texto) != null) {
            return
        }

        viewModelScope.launch {
            repository.agregarComentario(
                Comentario(
                    archivoPdf = archivoPdf,
                    autor = autor,
                    texto = texto.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }

    // Editar comentario con validación
    fun editarComentario(
        comentario: Comentario,
        nuevoTexto: String
    ) {
        if (Validaciones.validarComentario(nuevoTexto) != null) {
            return
        }

        viewModelScope.launch {
            repository.editarComentario(
                comentario.copy(
                    texto = nuevoTexto.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }

    // Eliminar comentario
    fun eliminarComentario(comentario: Comentario) {
        viewModelScope.launch {
            repository.eliminarComentario(comentario)
        }
    }
}
