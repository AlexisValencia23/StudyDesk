
package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {

    // Guardar una entrega y devolver su ID local
    @Insert
    suspend fun insertar(entrega: Entrega): Long

    // Obtener las entregas del usuario conectado
    @Query(
        "SELECT * FROM entregas " +
                "WHERE usuarioId = :usuarioId " +
                "ORDER BY id DESC"
    )
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>>

    // Asociar el ID de Spring Boot con la entrega local
    @Query(
        "UPDATE entregas " +
                "SET servidorId = :servidorId " +
                "WHERE id = :entregaId " +
                "AND usuarioId = :usuarioId " +
                "AND servidorId IS NULL"
    )
    suspend fun guardarServidorId(
        entregaId: Int,
        usuarioId: Int,
        servidorId: Long
    ): Int

    // Eliminar solamente entregas del usuario conectado
    @Query(
        "DELETE FROM entregas " +
                "WHERE id = :entregaId " +
                "AND usuarioId = :usuarioId"
    )
    suspend fun eliminarDelUsuario(
        entregaId: Int,
        usuarioId: Int
    ): Int
}
