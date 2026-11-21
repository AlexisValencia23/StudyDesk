
package com.example.studydesk.repository

import com.example.studydesk.model.Entrega
import com.example.studydesk.model.EntregaDao
import kotlinx.coroutines.flow.Flow

class EntregaRepository(
    private val entregaDao: EntregaDao
) {

    // Obtener las entregas del usuario conectado
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>> {
        return entregaDao.obtenerPorUsuario(usuarioId)
    }

    // Guardar una entrega en Room y devolver su ID local
    suspend fun agregarEntrega(
        entrega: Entrega
    ): Long {
        return entregaDao.insertar(entrega)
    }

    // Asociar el ID de Spring Boot con la entrega de Room
    suspend fun guardarServidorId(
        entregaId: Int,
        usuarioId: Int,
        servidorId: Long
    ): Int {
        return entregaDao.guardarServidorId(
            entregaId = entregaId,
            usuarioId = usuarioId,
            servidorId = servidorId
        )
    }

    // Eliminar solamente entregas del usuario conectado
    suspend fun eliminarEntrega(
        entregaId: Int,
        usuarioId: Int
    ) {
        entregaDao.eliminarDelUsuario(
            entregaId = entregaId,
            usuarioId = usuarioId
        )
    }
}
