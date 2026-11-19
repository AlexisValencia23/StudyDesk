
package com.example.studydesk.repository

import com.example.studydesk.network.ApiApunte
import com.example.studydesk.network.RetrofitClient
import com.example.studydesk.network.StudyDeskApi
import retrofit2.Response

class ApunteApiRepository(
    private val api: StudyDeskApi = RetrofitClient.api
) {

    // GET: Obtener apuntes por ID local
    // Se mantiene para compatibilidad
    suspend fun obtenerPorUsuario(
        usuarioId: Int
    ): List<ApiApunte> {
        return api.obtenerApuntes(usuarioId)
    }

    // GET: Obtener apuntes por correo
    suspend fun obtenerPorCorreo(
        correo: String
    ): List<ApiApunte> {
        return api.obtenerApuntesPorCorreo(
            correo.trim()
        )
    }

    // GET: Obtener un apunte por su ID
    suspend fun obtenerPorId(
        id: Long
    ): ApiApunte {
        return api.obtenerApunte(id)
    }

    // POST: Crear un apunte en Spring Boot
    suspend fun crear(
        apunte: ApiApunte
    ): ApiApunte {
        return api.crearApunte(apunte)
    }

    // PUT: Editar un apunte en Spring Boot
    suspend fun editar(
        id: Long,
        apunte: ApiApunte
    ): ApiApunte {
        return api.editarApunte(id, apunte)
    }

    // DELETE: Eliminar un apunte de Spring Boot
    suspend fun eliminar(
        id: Long
    ): Response<Void> {
        return api.eliminarApunte(id)
    }
}
