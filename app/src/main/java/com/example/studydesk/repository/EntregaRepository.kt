package com.example.studydesk.repository

import com.example.studydesk.model.Entrega
import com.example.studydesk.model.EntregaDao
import kotlinx.coroutines.flow.Flow

class EntregaRepository(private val entregaDao: EntregaDao) {

    fun obtenerTodas(): Flow<List<Entrega>> {
        return entregaDao.obtenerTodas()
    }

    suspend fun agregarEntrega(entrega: Entrega) {
        entregaDao.insertar(entrega)
    }

    // Eliminar una entrega
    suspend fun eliminarEntrega(entrega: Entrega) {
        entregaDao.eliminar(entrega)
    }
}