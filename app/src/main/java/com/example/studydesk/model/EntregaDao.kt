
package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {

    // Guardar una entrega
    @Insert
    suspend fun insertar(entrega: Entrega)

    // Obtener solo las entregas del usuario conectado
    @Query(
        "SELECT * FROM entregas " +
                "WHERE usuarioId = :usuarioId " +
                "ORDER BY id DESC"
    )
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>>

    // Eliminar solamente una entrega del usuario conectado
    @Query(
        "DELETE FROM entregas " +
                "WHERE id = :entregaId AND usuarioId = :usuarioId"
    )
    suspend fun eliminarDelUsuario(
        entregaId: Int,
        usuarioId: Int
    ): Int
}
