package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Comentario
import com.example.studydesk.repository.ComentarioRepository
import com.example.studydesk.util.FechaUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ComentarioViewModel(private val repository: ComentarioRepository) : ViewModel() {

    // Comentarios de un material; la lista se actualiza sola al agregar, editar o eliminar
    fun comentarios(archivoPdf: String): Flow<List<Comentario>> {
        return repository.obtenerPorMaterial(archivoPdf)
    }

    fun agregarComentario(archivoPdf: String, autor: String, texto: String) {
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

    fun editarComentario(comentario: Comentario, nuevoTexto: String) {
        viewModelScope.launch {
            repository.editarComentario(
                comentario.copy(texto = nuevoTexto.trim(), fecha = FechaUtil.hoy())
            )
        }
    }

    fun eliminarComentario(comentario: Comentario) {
        viewModelScope.launch {
            repository.eliminarComentario(comentario)
        }
    }
}