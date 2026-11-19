
package com.example.studydesk.repository

import com.example.studydesk.network.ApiEntrega
import com.example.studydesk.network.RetrofitClient
import com.example.studydesk.network.StudyDeskApi
import retrofit2.Response

class EntregaApiRepository(
    private val api: StudyDeskApi = RetrofitClient.api
) {

    // Obtener entregas de un usuario
    suspend fun obtenerPorUsuario(
        usuarioId: Int
    ): List<ApiEntrega> {
        return api.obtenerEntregas(usuarioId)
    }

    // Obtener una entrega por su ID
    suspend fun obtenerPorId(
        id: Long
    ): ApiEntrega {
        return api.obtenerEntrega(id)
    }

    // Crear una entrega en Spring Boot
    suspend fun crear(
        entrega: ApiEntrega
    ): ApiEntrega {
        return api.crearEntrega(entrega)
    }

    // Editar una entrega en Spring Boot
    suspend fun editar(
        id: Long,
        entrega: ApiEntrega
    ): ApiEntrega {
        return api.editarEntrega(id, entrega)
    }

    // Eliminar una entrega de Spring Boot
    suspend fun eliminar(
        id: Long
    ): Response<Void> {
        return api.eliminarEntrega(id)
    }
}
