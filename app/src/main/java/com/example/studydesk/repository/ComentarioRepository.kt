package com.example.studydesk.repository

import com.example.studydesk.model.Comentario
import com.example.studydesk.model.ComentarioDao
import kotlinx.coroutines.flow.Flow

class ComentarioRepository(private val comentarioDao: ComentarioDao) {

    fun obtenerPorMaterial(archivoPdf: String): Flow<List<Comentario>> {
        return comentarioDao.obtenerPorMaterial(archivoPdf)
    }

    suspend fun agregarComentario(comentario: Comentario) {
        comentarioDao.insertar(comentario)
    }

    suspend fun editarComentario(comentario: Comentario) {
        comentarioDao.actualizar(comentario)
    }

    suspend fun eliminarComentario(comentario: Comentario) {
        comentarioDao.eliminar(comentario)
    }
}