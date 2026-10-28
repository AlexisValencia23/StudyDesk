package com.example.studydesk.model

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComentarioDao {

    @Insert
    suspend fun insertar(comentario: Comentario)

    @Update
    suspend fun actualizar(comentario: Comentario)

    @Delete
    suspend fun eliminar(comentario: Comentario)

    // Los comentarios de un solo material, del más nuevo al más antiguo
    @Query("SELECT * FROM comentarios WHERE archivoPdf = :archivoPdf ORDER BY id DESC")
    fun obtenerPorMaterial(archivoPdf: String): Flow<List<Comentario>>
}