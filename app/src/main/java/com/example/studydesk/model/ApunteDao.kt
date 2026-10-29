
package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApunteDao {

    // Guardar un apunte
    @Insert
    suspend fun insertar(apunte: Apunte)

    // Obtener solamente los apuntes de este usuario
    @Query(
        "SELECT * FROM apuntes " +
                "WHERE usuarioId = :usuarioId " +
                "ORDER BY id DESC"
    )
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<Apunte>>

    // Editar solamente si el apunte pertenece al usuario
    @Query(
        "UPDATE apuntes " +
                "SET titulo = :titulo, contenido = :contenido, fecha = :fecha " +
                "WHERE id = :apunteId AND usuarioId = :usuarioId"
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
                "WHERE id = :apunteId AND usuarioId = :usuarioId"
    )
    suspend fun eliminarDelUsuario(
        apunteId: Int,
        usuarioId: Int
    ): Int
}
