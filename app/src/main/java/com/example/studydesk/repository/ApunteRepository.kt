
package com.example.studydesk.repository

import com.example.studydesk.model.Apunte
import com.example.studydesk.model.ApunteDao
import kotlinx.coroutines.flow.Flow

class ApunteRepository(
    private val apunteDao: ApunteDao
) {

    // Obtener apuntes de una cuenta
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<Apunte>> {
        return apunteDao.obtenerPorUsuario(usuarioId)
    }

    // Guardar apunte
    suspend fun agregarApunte(apunte: Apunte) {
        apunteDao.insertar(apunte)
    }

    // Editar solamente apuntes propios
    suspend fun editarApunte(
        apunte: Apunte,
        usuarioId: Int
    ) {
        apunteDao.actualizarDelUsuario(
            apunteId = apunte.id,
            usuarioId = usuarioId,
            titulo = apunte.titulo,
            contenido = apunte.contenido,
            fecha = apunte.fecha
        )
    }

    // Eliminar solamente apuntes propios
    suspend fun eliminarApunte(
        apunteId: Int,
        usuarioId: Int
    ) {
        apunteDao.eliminarDelUsuario(
            apunteId = apunteId,
            usuarioId = usuarioId
        )
    }
}
