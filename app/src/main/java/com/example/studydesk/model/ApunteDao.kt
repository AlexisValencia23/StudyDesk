package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApunteDao {

    @Insert
    suspend fun insertar(apunte: Apunte)

    // Para poder editar un apunte que ya existe
    @Update
    suspend fun actualizar(apunte: Apunte)

    @Query("SELECT * FROM apuntes ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<Apunte>>
}