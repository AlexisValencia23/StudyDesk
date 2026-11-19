
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

    // GET: Apuntes por ID local
    @GET("api/apuntes/usuario/{usuarioId}")
    suspend fun obtenerApuntes(
        @Path("usuarioId") usuarioId: Int
    ): List<ApiApunte>

    // GET: Apuntes por correo
    @GET("api/apuntes/usuario/correo/{correo}")
    suspend fun obtenerApuntesPorCorreo(
        @Path("correo") correo: String
    ): List<ApiApunte>

    // GET: Apunte por ID
    @GET("api/apuntes/{id}")
    suspend fun obtenerApunte(
        @Path("id") id: Long
    ): ApiApunte

    // POST: Crear apunte
    @POST("api/apuntes")
    suspend fun crearApunte(
        @Body apunte: ApiApunte
    ): ApiApunte

    // PUT: Editar apunte
    @PUT("api/apuntes/{id}")
    suspend fun editarApunte(
        @Path("id") id: Long,
        @Body apunte: ApiApunte
    ): ApiApunte

    // DELETE: Eliminar apunte
    @DELETE("api/apuntes/{id}")
    suspend fun eliminarApunte(
        @Path("id") id: Long
    ): Response<Void>


    // ENTREGAS

    // GET: Entregas por ID local
    @GET("api/entregas/usuario/{usuarioId}")
    suspend fun obtenerEntregas(
        @Path("usuarioId") usuarioId: Int
    ): List<ApiEntrega>

    // GET: Entregas por correo
    @GET("api/entregas/usuario/correo/{correo}")
    suspend fun obtenerEntregasPorCorreo(
        @Path("correo") correo: String
    ): List<ApiEntrega>

    // GET: Entrega por ID
    @GET("api/entregas/{id}")
    suspend fun obtenerEntrega(
        @Path("id") id: Long
    ): ApiEntrega

    // POST: Crear entrega
    @POST("api/entregas")
    suspend fun crearEntrega(
        @Body entrega: ApiEntrega
    ): ApiEntrega

    // PUT: Editar entrega
    @PUT("api/entregas/{id}")
    suspend fun editarEntrega(
        @Path("id") id: Long,
        @Body entrega: ApiEntrega
    ): ApiEntrega

    // DELETE: Eliminar entrega
    @DELETE("api/entregas/{id}")
    suspend fun eliminarEntrega(
        @Path("id") id: Long
    ): Response<Void>
}


// MODELO API: APUNTE

data class ApiApunte(
    val materia: String,
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val usuarioId: Int? = null,
    val correoUsuario: String? = null,
    val id: Long? = null
)


// MODELO API: ENTREGA

data class ApiEntrega(
    val materia: String,
    val titulo: String,
    val fecha: String,
    val usuarioId: Int? = null,
    val correoUsuario: String? = null,
    val id: Long? = null
)
