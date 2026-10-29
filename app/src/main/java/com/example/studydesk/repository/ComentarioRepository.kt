
package com.example.studydesk.repository

import com.example.studydesk.model.Comentario
import com.example.studydesk.model.ComentarioDao
import kotlinx.coroutines.flow.Flow

class ComentarioRepository(
    private val comentarioDao: ComentarioDao
) {

    // Obtener comentarios de un material
    fun obtenerPorMaterial(
        archivoPdf: String
    ): Flow<List<Comentario>> {
        return comentarioDao.obtenerPorMaterial(archivoPdf)
    }

    // Crear comentario
    suspend fun agregarComentario(comentario: Comentario) {
        comentarioDao.insertar(comentario)
    }

    // Editar comentario del usuario
    suspend fun editarComentario(
        comentario: Comentario,
        usuarioId: Int
    ) {
        comentarioDao.actualizarDelAutor(
            comentarioId = comentario.id,
            autorId = usuarioId,
            texto = comentario.texto,
            fecha = comentario.fecha
        )
    }

    // Eliminar comentario del usuario
    suspend fun eliminarComentario(
        comentarioId: Int,
        usuarioId: Int
    ) {
        comentarioDao.eliminarDelAutor(
            comentarioId = comentarioId,
            autorId = usuarioId
        )
    }
}
