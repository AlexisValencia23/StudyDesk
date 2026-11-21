
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

    // Obtener entregas del usuario conectado
    @Query(
        "SELECT * FROM entregas " +
                "WHERE usuarioId = :usuarioId " +
                "ORDER BY id DESC"
    )
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>>

    // Obtener entregas pendientes de sincronizacion
    @Query(
        "SELECT * FROM entregas " +
                "WHERE usuarioId = :usuarioId " +
                "AND servidorId IS NULL " +
                "ORDER BY id ASC"
    )
    suspend fun obtenerPendientesPorUsuario(
        usuarioId: Int
    ): List<Entrega>

    // Guardar el ID de Spring Boot en Room
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
