package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import androidx.room3.Delete

@Dao
interface EntregaDao {

    @Insert
    suspend fun insertar(entrega: Entrega)

    // Devuelve la lista y se actualiza sola cuando se agrega una entrega
    @Query("SELECT * FROM entregas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Entrega>>

    // Eliminar una entrega de Room
    @Delete
    suspend fun eliminar(entrega: Entrega)
}