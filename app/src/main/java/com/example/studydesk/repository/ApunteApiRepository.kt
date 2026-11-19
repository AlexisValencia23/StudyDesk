
package com.example.studydesk.repository

import com.example.studydesk.network.ApiApunte
import com.example.studydesk.network.RetrofitClient
import com.example.studydesk.network.StudyDeskApi
import retrofit2.Response

class ApunteApiRepository(
    private val api: StudyDeskApi = RetrofitClient.api
) {

    // Obtener apuntes de un usuario
    suspend fun obtenerPorUsuario(
        usuarioId: Int
    ): List<ApiApunte> {
        return api.obtenerApuntes(usuarioId)
    }

    // Obtener un apunte por su ID
    suspend fun obtenerPorId(
        id: Long
    ): ApiApunte {
        return api.obtenerApunte(id)
    }

    // Guardar un apunte en Spring Boot
    suspend fun crear(
        apunte: ApiApunte
    ): ApiApunte {
        return api.crearApunte(apunte)
    }

    // Editar un apunte en Spring Boot
    suspend fun editar(
        id: Long,
        apunte: ApiApunte
    ): ApiApunte {
        return api.editarApunte(id, apunte)
    }

    // Eliminar un apunte de Spring Boot
    suspend fun eliminar(
        id: Long
    ): Response<Void> {
        return api.eliminarApunte(id)
    }
}
