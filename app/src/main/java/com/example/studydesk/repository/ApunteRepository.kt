package com.example.studydesk.repository

import com.example.studydesk.model.Apunte
import com.example.studydesk.model.ApunteDao
import kotlinx.coroutines.flow.Flow

class ApunteRepository(private val apunteDao: ApunteDao) {

    fun obtenerTodos(): Flow<List<Apunte>> {
        return apunteDao.obtenerTodos()
    }

    suspend fun agregarApunte(apunte: Apunte) {
        apunteDao.insertar(apunte)
    }

    suspend fun editarApunte(apunte: Apunte) {
        apunteDao.actualizar(apunte)
    }
}