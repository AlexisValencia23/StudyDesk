
package com.example.studydesk.repository

import com.example.studydesk.model.Apunte
import com.example.studydesk.model.ApunteDao
import kotlinx.coroutines.flow.Flow

class ApunteRepository(
    private val apunteDao: ApunteDao
) {

    // Obtener los apuntes del usuario conectado
    fun obtenerPorUsuario(
        usuarioId: Int
    ): Flow<List<Apunte>> {
        return apunteDao.obtenerPorUsuario(usuarioId)
    }

    // NUEVO: Obtener apuntes sin ID del servidor
    suspend fun obtenerPendientesPorUsuario(
        usuarioId: Int
    ): List<Apunte> {
        return apunteDao.obtenerPendientesPorUsuario(
            usuarioId
        )
    }

    // Guardar un apunte en Room y devolver su ID local
    suspend fun agregarApunte(
        apunte: Apunte
    ): Long {
        return apunteDao.insertar(apunte)
    }

    // Asociar el ID de Spring Boot al apunte de Room
    suspend fun guardarServidorId(
        apunteId: Int,
        usuarioId: Int,
        servidorId: Long
    ): Int {
        return apunteDao.guardarServidorId(
            apunteId = apunteId,
            usuarioId = usuarioId,
            servidorId = servidorId
        )
    }

    // Editar solamente apuntes del usuario conectado
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

    // Eliminar solamente apuntes del usuario conectado
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
