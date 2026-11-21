
package com.example.studydesk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Entrega
import com.example.studydesk.network.ApiEntrega
import com.example.studydesk.repository.EntregaApiRepository
import com.example.studydesk.repository.EntregaRepository
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class EntregaViewModel(
    private val repository: EntregaRepository,
    private val apiRepository: EntregaApiRepository = EntregaApiRepository()
) : ViewModel() {

    // Usuario conectado en Room
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    // Correo para consultar Spring Boot
    private val correoActivo = MutableStateFlow<String?>(null)

    // Mensajes de sincronizacion
    private val _mensajeSincronizacion =
        MutableStateFlow<String?>(null)

    val mensajeSincronizacion: StateFlow<String?> =
        _mensajeSincronizacion

    // Estado de sincronizacion de pendientes
    private val _sincronizandoPendientes =
        MutableStateFlow(false)

    val sincronizandoPendientes: StateFlow<Boolean> =
        _sincronizandoPendientes

    // Entregas locales del usuario
    @OptIn(ExperimentalCoroutinesApi::class)
    val entregas: StateFlow<List<Entrega>> = usuarioActivo
        .flatMapLatest { usuarioId ->

            if (usuarioId == null) {
                flowOf(emptyList<Entrega>())
            } else {
                repository.obtenerPorUsuario(usuarioId)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Establecer usuario activo
    fun establecerUsuario(usuarioId: Int?) {

        if (usuarioActivo.value != usuarioId) {
            correoActivo.value = null
            _mensajeSincronizacion.value = null
        }

        usuarioActivo.value = usuarioId
    }

    // Establecer correo activo
    fun establecerCorreo(correo: String?) {

        if (usuarioActivo.value == null) {
            correoActivo.value = null
            return
        }

        correoActivo.value = correo
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
    }

    // CREAR ENTREGA EN ROOM Y SPRING BOOT
    fun agregarEntrega(
        materia: String,
        titulo: String,
        fecha: String
    ) {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (
            Validaciones.validarMateriaEntrega(materia) != null ||
            Validaciones.validarTituloEntrega(titulo) != null ||
            Validaciones.validarFechaEntrega(fecha) != null
        ) {
            return
        }

        val nuevaEntrega = Entrega(
            materia = materia.trim(),
            titulo = titulo.trim(),
            fecha = fecha.trim(),
            usuarioId = usuarioId
        )

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            // PASO 1: Guardar en Room
            val idLocal = repository.agregarEntrega(nuevaEntrega)

            Log.d(
                "StudyDeskSync",
                "Entrega guardada en Room. ID local: $idLocal"
            )

            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Entrega guardada en Room, pero falta el correo del servidor."

                return@launch
            }

            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            // PASO 2: Crear en Spring Boot
            try {

                val entregaServidor = ApiEntrega(
                    materia = nuevaEntrega.materia,
                    titulo = nuevaEntrega.titulo,
                    fecha = nuevaEntrega.fecha,
                    correoUsuario = correo
                )

                val respuesta =
                    apiRepository.crear(entregaServidor)

                val idServidor = respuesta.id

                if (idServidor == null) {

                    _mensajeSincronizacion.value =
                        "El servidor recibio la entrega, pero no devolvio su ID."

                    Log.w(
                        "StudyDeskSync",
                        "Spring Boot no devolvio el ID de la entrega"
                    )

                    return@launch
                }

                // PASO 3: Vincular IDs
                val actualizados =
                    repository.guardarServidorId(
                        entregaId = idLocal.toInt(),
                        usuarioId = usuarioId,
                        servidorId = idServidor
                    )

                if (actualizados == 1) {

                    if (
                        usuarioActivo.value == usuarioId &&
                        correoActivo.value == correo
                    ) {
                        _mensajeSincronizacion.value =
                            "Entrega guardada y vinculada con Spring Boot."
                    }

                    Log.d(
                        "StudyDeskSync",
                        "Entrega vinculada. Room: $idLocal, " +
                                "Spring Boot: $idServidor"
                    )

                } else {

                    _mensajeSincronizacion.value =
                        "Entrega enviada al servidor, pero no se pudo vincular en Room."

                    Log.w(
                        "StudyDeskSync",
                        "No se pudo guardar servidorId de la entrega"
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "La entrega se guardo en Room, pero fallo la sincronizacion."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al sincronizar la entrega",
                    e
                )
            }
        }
    }

    // ELIMINAR ENTREGA EN ROOM Y SPRING BOOT
    fun eliminarEntrega(entrega: Entrega) {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (entrega.usuarioId != usuarioId) {
            return
        }

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            val idServidor = entrega.servidorId

            // Si no esta vinculada, eliminar solo de Room
            if (idServidor == null) {

                repository.eliminarEntrega(
                    entregaId = entrega.id,
                    usuarioId = usuarioId
                )

                Log.d(
                    "StudyDeskSync",
                    "Entrega sin servidorId eliminada de Room. ID: ${entrega.id}"
                )

                _mensajeSincronizacion.value =
                    "Entrega local eliminada."

                return@launch
            }

            // Si esta vinculada, necesitamos el correo
            if (correo == null) {

                _mensajeSincronizacion.value =
                    "No se pudo eliminar: falta el correo del usuario."

                Log.w(
                    "StudyDeskSync",
                    "Eliminacion cancelada: falta el correo"
                )

                return@launch
            }

            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            try {

                // PASO 1: Consultar registro remoto
                val registroServidor = try {

                    apiRepository.obtenerPorId(idServidor)

                } catch (e: HttpException) {

                    if (e.code() == 404) {
                        null
                    } else {
                        throw e
                    }
                }

                // PASO 2: Verificar correo remoto
                if (registroServidor != null) {

                    val correoServidor =
                        registroServidor.correoUsuario
                            ?.trim()
                            ?.lowercase(Locale.ROOT)

                    if (correoServidor != correo) {

                        _mensajeSincronizacion.value =
                            "No se elimino: el registro remoto pertenece a otro correo."

                        Log.w(
                            "StudyDeskSync",
                            "Eliminacion cancelada: correo remoto no coincide"
                        )

                        return@launch
                    }
                }

                if (
                    usuarioActivo.value != usuarioId ||
                    correoActivo.value != correo
                ) {
                    return@launch
                }

                // PASO 3: Eliminar del servidor
                if (registroServidor != null) {

                    val respuesta =
                        apiRepository.eliminar(idServidor)

                    if (
                        !respuesta.isSuccessful &&
                        respuesta.code() != 404
                    ) {

                        _mensajeSincronizacion.value =
                            "No se pudo eliminar en Spring Boot. Se conservo en Room."

                        Log.w(
                            "StudyDeskSync",
                            "Error HTTP al eliminar entrega: ${respuesta.code()}"
                        )

                        return@launch
                    }

                    Log.d(
                        "StudyDeskSync",
                        "Entrega eliminada en Spring Boot. ID: $idServidor"
                    )

                } else {

                    Log.w(
                        "StudyDeskSync",
                        "La entrega ya no existe en Spring Boot. ID: $idServidor"
                    )
                }

                // PASO 4: Eliminar de Room
                repository.eliminarEntrega(
                    entregaId = entrega.id,
                    usuarioId = usuarioId
                )

                Log.d(
                    "StudyDeskSync",
                    "Entrega eliminada en Room. ID: ${entrega.id}"
                )

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Entrega eliminada de Room y Spring Boot."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "No se completo la eliminacion. Revisa Spring Boot."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al eliminar la entrega",
                    e
                )
            }
        }
    }

    // NUEVO: SINCRONIZAR ENTREGAS PENDIENTES
    fun sincronizarPendientes() {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (correo == null) {
            _mensajeSincronizacion.value =
                "No se puede sincronizar: falta el correo."
            return
        }

        // Evitar sincronizaciones manuales simultaneas
        if (_sincronizandoPendientes.value) return

        _sincronizandoPendientes.value = true

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            try {

                // PASO 1: Obtener entregas locales
                val locales =
                    repository.obtenerPorUsuario(usuarioId).first()

                // PASO 2: Obtener entregas pendientes
                val pendientes =
                    repository.obtenerPendientesPorUsuario(usuarioId)

                if (pendientes.isEmpty()) {

                    _mensajeSincronizacion.value =
                        "No hay entregas pendientes de sincronizacion."

                    Log.d(
                        "StudyDeskSync",
                        "No hay entregas pendientes"
                    )

                    return@launch
                }

                // PASO 3: Consultar el servidor
                val remotas =
                    apiRepository.obtenerPorCorreo(correo)
                        .toMutableList()

                // IDs remotos ya asociados a Room
                val idsUtilizados = locales
                    .mapNotNull { it.servidorId }
                    .toMutableSet()

                var vinculadas = 0
                var omitidas = 0

                for (entrega in pendientes) {

                    // Cancelar si cambio la cuenta
                    if (
                        usuarioActivo.value != usuarioId ||
                        correoActivo.value != correo
                    ) {
                        return@launch
                    }

                    // PASO 4: Buscar coincidencias exactas
                    val coincidencias = remotas.filter {
                        it.materia == entrega.materia &&
                                it.titulo == entrega.titulo &&
                                it.fecha == entrega.fecha &&
                                it.correoUsuario
                                    ?.trim()
                                    ?.lowercase(Locale.ROOT) == correo
                    }

                    // Detectar posibles duplicados
                    val similares = remotas.filter {
                        it.materia == entrega.materia &&
                                it.titulo == entrega.titulo
                    }

                    val idExistente =
                        coincidencias.singleOrNull()?.id

                    val idServidor = when {

                        // Coincidencia unica sin usar
                        idExistente != null &&
                                idExistente !in idsUtilizados -> {

                            Log.d(
                                "StudyDeskSync",
                                "Entrega encontrada en servidor. " +
                                        "Room: ${entrega.id}, " +
                                        "Spring Boot: $idExistente"
                            )

                            idExistente
                        }

                        // Hay coincidencias ambiguas:
                        // no generar otra copia
                        coincidencias.isNotEmpty() ||
                                similares.isNotEmpty() -> {

                            omitidas++

                            Log.w(
                                "StudyDeskSync",
                                "Entrega pendiente de revision: ${entrega.id}"
                            )

                            continue
                        }

                        // No hay coincidencias:
                        // crear una nueva entrega remota
                        else -> {

                            val respuesta =
                                apiRepository.crear(
                                    ApiEntrega(
                                        materia = entrega.materia,
                                        titulo = entrega.titulo,
                                        fecha = entrega.fecha,
                                        correoUsuario = correo
                                    )
                                )

                            remotas.add(respuesta)

                            respuesta.id
                        }
                    }

                    if (idServidor == null) {

                        omitidas++

                        Log.w(
                            "StudyDeskSync",
                            "No se recibio ID para entrega ${entrega.id}"
                        )

                        continue
                    }

                    // PASO 5: Asociar ID remoto en Room
                    val actualizados =
                        repository.guardarServidorId(
                            entregaId = entrega.id,
                            usuarioId = usuarioId,
                            servidorId = idServidor
                        )

                    if (actualizados == 1) {

                        idsUtilizados.add(idServidor)
                        vinculadas++

                        Log.d(
                            "StudyDeskSync",
                            "Entrega pendiente vinculada. " +
                                    "Room: ${entrega.id}, " +
                                    "Spring Boot: $idServidor"
                        )

                    } else {

                        omitidas++

                        Log.w(
                            "StudyDeskSync",
                            "No se pudo vincular entrega ${entrega.id}"
                        )
                    }
                }

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Entregas vinculadas: $vinculadas. " +
                                "Pendientes de revision: $omitidas."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "No se completo la sincronizacion de entregas. Revisa la conexion."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al sincronizar entregas pendientes",
                    e
                )

            } finally {

                _sincronizandoPendientes.value = false
            }
        }
    }
}
