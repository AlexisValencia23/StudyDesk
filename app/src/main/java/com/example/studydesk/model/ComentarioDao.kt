
package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    // Guardar comentario
    @Insert
    suspend fun insertar(comentario: Comentario)

    // Todos pueden ver los comentarios de un material
    @Query(
        "SELECT * FROM comentarios " +
                "WHERE archivoPdf = :archivoPdf " +
                "ORDER BY id DESC"
    )
    fun obtenerPorMaterial(
        archivoPdf: String
    ): Flow<List<Comentario>>

    // Editar solamente comentarios propios
    @Query(
        "UPDATE comentarios " +
                "SET texto = :texto, fecha = :fecha " +
                "WHERE id = :comentarioId AND autorId = :autorId"
    )
    suspend fun actualizarDelAutor(
        comentarioId: Int,
        autorId: Int,
        texto: String,
        fecha: String
    ): Int

    // Eliminar solamente comentarios propios
    @Query(
        "DELETE FROM comentarios " +
                "WHERE id = :comentarioId AND autorId = :autorId"
    )
    suspend fun eliminarDelAutor(
        comentarioId: Int,
        autorId: Int
    ): Int
}
