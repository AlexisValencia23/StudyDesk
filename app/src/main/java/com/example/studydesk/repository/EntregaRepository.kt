
package com.example.studydesk.repository

import com.example.studydesk.model.Entrega
import com.example.studydesk.model.EntregaDao
import kotlinx.coroutines.flow.Flow

class EntregaRepository(
    private val entregaDao: EntregaDao
) {

    // Obtener entregas del usuario conectado
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>> {
        return entregaDao.obtenerPorUsuario(usuarioId)
    }

    // Obtener entregas pendientes de sincronizacion
    suspend fun obtenerPendientesPorUsuario(
        usuarioId: Int
    ): List<Entrega> {
        return entregaDao.obtenerPendientesPorUsuario(
            usuarioId
        )
    }

    // Guardar entrega en Room
    suspend fun agregarEntrega(
        entrega: Entrega
    ): Long {
        return entregaDao.insertar(entrega)
    }

    // Vincular entrega local con Spring Boot
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

    // Eliminar entrega del usuario conectado
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
