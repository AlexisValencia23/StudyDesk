
package com.example.studydesk.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StudyDeskApi {

    // APUNTES

    @GET("api/apuntes/usuario/{usuarioId}")
    suspend fun obtenerApuntes(
        @Path("usuarioId") usuarioId: Int
    ): List<ApiApunte>

    @GET("api/apuntes/{id}")
    suspend fun obtenerApunte(
        @Path("id") id: Long
    ): ApiApunte

    @POST("api/apuntes")
    suspend fun crearApunte(
        @Body apunte: ApiApunte
    ): ApiApunte

    @PUT("api/apuntes/{id}")
    suspend fun editarApunte(
        @Path("id") id: Long,
        @Body apunte: ApiApunte
    ): ApiApunte

    @DELETE("api/apuntes/{id}")
    suspend fun eliminarApunte(
        @Path("id") id: Long
    ): Response<Void>

    // ENTREGAS

    @GET("api/entregas/usuario/{usuarioId}")
    suspend fun obtenerEntregas(
        @Path("usuarioId") usuarioId: Int
    ): List<ApiEntrega>

    @GET("api/entregas/{id}")
    suspend fun obtenerEntrega(
        @Path("id") id: Long
    ): ApiEntrega

    @POST("api/entregas")
    suspend fun crearEntrega(
        @Body entrega: ApiEntrega
    ): ApiEntrega

    @PUT("api/entregas/{id}")
    suspend fun editarEntrega(
        @Path("id") id: Long,
        @Body entrega: ApiEntrega
    ): ApiEntrega

    @DELETE("api/entregas/{id}")
    suspend fun eliminarEntrega(
        @Path("id") id: Long
    ): Response<Void>
}

// Datos de apuntes recibidos desde Spring Boot

data class ApiApunte(
    val materia: String,
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val usuarioId: Int,
    val id: Long? = null
)

// Datos de entregas recibidos desde Spring Boot

data class ApiEntrega(
    val materia: String,
    val titulo: String,
    val fecha: String,
    val usuarioId: Int,
    val id: Long? = null
)
