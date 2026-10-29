
package com.example.studydesk.repository

import com.example.studydesk.model.Entrega
import com.example.studydesk.model.EntregaDao
import kotlinx.coroutines.flow.Flow

class EntregaRepository(
    private val entregaDao: EntregaDao
) {

    // Obtener las entregas de una cuenta
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Entrega>> {
        return entregaDao.obtenerPorUsuario(usuarioId)
    }

    // Guardar una entrega nueva
    suspend fun agregarEntrega(entrega: Entrega) {
        entregaDao.insertar(entrega)
    }

    // Eliminar una entrega propia
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
