package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntregaDao {

    @Insert
    suspend fun insertar(entrega: Entrega)

    // Devuelve la lista y se actualiza sola cuando se agrega una entrega
    @Query("SELECT * FROM entregas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Entrega>>
}