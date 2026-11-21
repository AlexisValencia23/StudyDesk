
package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApunteDao {

    // Guardar un apunte y devolver su ID local
    @Insert
    suspend fun insertar(apunte: Apunte): Long

    // Obtener los apuntes del usuario conectado
    @Query(
        "SELECT * FROM apuntes " +
                "WHERE usuarioId = :usuarioId " +
                "ORDER BY id DESC"
    )
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Apunte>>

    // Asociar el ID de Spring Boot con el apunte de Room
    @Query(
        "UPDATE apuntes " +
                "SET servidorId = :servidorId " +
                "WHERE id = :apunteId " +
                "AND usuarioId = :usuarioId " +
                "AND servidorId IS NULL"
    )
    suspend fun guardarServidorId(
        apunteId: Int,
        usuarioId: Int,
        servidorId: Long
    ): Int

    // Editar solamente apuntes del usuario conectado
    @Query(
        "UPDATE apuntes " +
                "SET titulo = :titulo, " +
                "contenido = :contenido, " +
                "fecha = :fecha " +
                "WHERE id = :apunteId " +
                "AND usuarioId = :usuarioId"
    )
    suspend fun actualizarDelUsuario(
        apunteId: Int,
        usuarioId: Int,
        titulo: String,
        contenido: String,
        fecha: String
    ): Int

    // Eliminar solamente apuntes del usuario conectado
    @Query(
        "DELETE FROM apuntes " +
                "WHERE id = :apunteId " +
                "AND usuarioId = :usuarioId"
    )
    suspend fun eliminarDelUsuario(
        apunteId: Int,
        usuarioId: Int
    ): Int
}
