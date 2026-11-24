
package com.example.studydesk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Entrega
import com.example.studydesk.network.ApiEntrega
import com.example.studydesk.repository.EntregaApiRepository
import com.example.studydesk.repository.EntregaRepository
import com.example.studydesk.util.IdentificadorSync
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
    private val apiRepository: EntregaApiRepository = EntregaApiRepository(),
    private val identificadorSync: IdentificadorSync
) : ViewModel() {

    private val usuarioActivo = MutableStateFlow<Int?>(null)
    private val correoActivo = MutableStateFlow<String?>(null)

    private val _mensajeSincronizacion =
        MutableStateFlow<String?>(null)

    val mensajeSincronizacion: StateFlow<String?> =
        _mensajeSincronizacion

    private val _sincronizandoPendientes =
        MutableStateFlow(false)

    val sincronizandoPendientes: StateFlow<Boolean> =
        _sincronizandoPendientes

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

    fun establecerUsuario(usuarioId: Int?) {
        if (usuarioActivo.value != usuarioId) {
            correoActivo.value = null
            _mensajeSincronizacion.value = null
        }

        usuarioActivo.value = usuarioId
    }

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

    private fun cuentaActiva(
        usuarioId: Int,
        correo: String
    ): Boolean {
        return usuarioActivo.value == usuarioId &&
                correoActivo.value == correo
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

            val idLocal = repository.agregarEntrega(nuevaEntrega)
            val idCliente =
                identificadorSync.entrega(idLocal.toInt())


            if (correo == null) {
                _mensajeSincronizacion.value =
                    "Entrega guardada en Room, pero falta el correo."
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                val respuesta = apiRepository.crear(
                    ApiEntrega(
                        materia = nuevaEntrega.materia,
                        titulo = nuevaEntrega.titulo,
                        fecha = nuevaEntrega.fecha,
                        correoUsuario = correo,
                        idCliente = idCliente
                    )
                )

                val idServidor = respuesta.id

                if (
                    idServidor == null ||
                    respuesta.idCliente != idCliente ||
                    respuesta.correoUsuario
                        ?.trim()
                        ?.lowercase(Locale.ROOT) != correo
                ) {
                    if (cuentaActiva(usuarioId, correo)) {
                        _mensajeSincronizacion.value =
                            "La respuesta del servidor necesita revision."
                    }

                    Log.w(
                        "StudyDeskSync",
                        "Respuesta no valida para entrega Room: $idLocal"
                    )
                    return@launch
                }

                if (!cuentaActiva(usuarioId, correo)) {
                    return@launch
                }

                val actualizados = repository.guardarServidorId(
                    entregaId = idLocal.toInt(),
                    usuarioId = usuarioId,
                    servidorId = idServidor
                )

                if (actualizados == 1) {
                    _mensajeSincronizacion.value =
                        "Entrega guardada y vinculada con Spring Boot."

                } else {
                    _mensajeSincronizacion.value =
                        "Entrega enviada, pero no se pudo vincular en Room."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Entrega guardada en Room. Sincronizacion pendiente."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al sincronizar la entrega",
                    e
                )
            }
        }
    }

    // ELIMINAR ENTREGA
    fun eliminarEntrega(entrega: Entrega) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (entrega.usuarioId != usuarioId) {
            return
        }

        viewModelScope.launch {
            _mensajeSincronizacion.value = null

            val idServidor = entrega.servidorId

            if (idServidor == null) {
                repository.eliminarEntrega(
                    entregaId = entrega.id,
                    usuarioId = usuarioId
                )


                _mensajeSincronizacion.value =
                    "Entrega local eliminada."
                return@launch
            }

            if (correo == null) {
                _mensajeSincronizacion.value =
                    "No se pudo eliminar: falta el correo."
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                val registroServidor = try {
                    apiRepository.obtenerPorId(idServidor)
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        null
                    } else {
                        throw e
                    }
                }

                if (registroServidor != null) {
                    val correoServidor =
                        registroServidor.correoUsuario
                            ?.trim()
                            ?.lowercase(Locale.ROOT)

                    if (correoServidor != correo) {
                        _mensajeSincronizacion.value =
                            "No se elimino: el registro pertenece a otro correo."
                        return@launch
                    }
                }

                if (!cuentaActiva(usuarioId, correo)) {
                    return@launch
                }

                if (registroServidor != null) {
                    val respuesta =
                        apiRepository.eliminar(idServidor)

                    if (
                        !respuesta.isSuccessful &&
                        respuesta.code() != 404
                    ) {
                        _mensajeSincronizacion.value =
                            "No se pudo eliminar en Spring Boot."
                        return@launch
                    }
                }

                repository.eliminarEntrega(
                    entregaId = entrega.id,
                    usuarioId = usuarioId
                )

                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Entrega eliminada de Room y Spring Boot."
                }


            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
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

    // SINCRONIZAR ENTREGAS PENDIENTES
    fun sincronizarPendientes() {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (correo == null) {
            _mensajeSincronizacion.value =
                "No se puede sincronizar: falta el correo."
            return
        }

        if (_sincronizandoPendientes.value) {
            return
        }

        _sincronizandoPendientes.value = true

        viewModelScope.launch {
            var vinculadas = 0
            var pendientesRevision = 0

            _mensajeSincronizacion.value = null

            try {
                val locales =
                    repository.obtenerPorUsuario(usuarioId).first()

                val pendientes =
                    repository.obtenerPendientesPorUsuario(usuarioId)

                if (pendientes.isEmpty()) {
                    if (cuentaActiva(usuarioId, correo)) {
                        _mensajeSincronizacion.value =
                            "No hay entregas pendientes de sincronizacion."
                    }

                    return@launch
                }

                // Primero revisar los datos del servidor
                val remotas =
                    apiRepository.obtenerPorCorreo(correo)
                        .toMutableList()

                val idsUtilizados = locales
                    .mapNotNull { it.servidorId }
                    .toMutableSet()

                for (entrega in pendientes) {
                    if (!cuentaActiva(usuarioId, correo)) {
                        return@launch
                    }

                    val idCliente =
                        identificadorSync.entrega(entrega.id)

                    // Buscar por identificador estable
                    val porIdentificador = remotas.filter {
                        it.idCliente == idCliente
                    }

                    if (porIdentificador.size > 1) {
                        pendientesRevision++

                        Log.w(
                            "StudyDeskSync",
                            "Identificador duplicado: $idCliente"
                        )
                        continue
                    }

                    // Compatibilidad con registros antiguos
                    val antiguas = remotas.filter {
                        it.idCliente == null &&
                                it.materia == entrega.materia &&
                                it.titulo == entrega.titulo &&
                                it.fecha == entrega.fecha &&
                                it.correoUsuario
                                    ?.trim()
                                    ?.lowercase(Locale.ROOT) == correo &&
                                it.id !in idsUtilizados
                    }

                    val respuesta: ApiEntrega
                    var registroAntiguo = false

                    if (porIdentificador.size == 1) {
                        // Ya recibido anteriormente
                        respuesta = porIdentificador.first()

                    } else if (antiguas.size == 1) {
                        // Posible registro anterior a idCliente
                        respuesta = antiguas.first()
                        registroAntiguo = true

                    } else if (antiguas.size > 1) {
                        pendientesRevision++

                        Log.w(
                            "StudyDeskSync",
                            "Entrega antigua ambigua. Room: ${entrega.id}"
                        )
                        continue

                    } else {
                        // POST repetido con el mismo idCliente
                        respuesta = apiRepository.crear(
                            ApiEntrega(
                                materia = entrega.materia,
                                titulo = entrega.titulo,
                                fecha = entrega.fecha,
                                correoUsuario = correo,
                                idCliente = idCliente
                            )
                        )

                        remotas.add(respuesta)
                    }

                    val correoServidor = respuesta.correoUsuario
                        ?.trim()
                        ?.lowercase(Locale.ROOT)

                    val datosCoinciden =
                        respuesta.materia == entrega.materia &&
                                respuesta.titulo == entrega.titulo &&
                                respuesta.fecha == entrega.fecha

                    val identificadorCorrecto =
                        if (registroAntiguo) {
                            respuesta.idCliente == null
                        } else {
                            respuesta.idCliente == idCliente
                        }

                    val idServidor = respuesta.id

                    if (
                        correoServidor != correo ||
                        !datosCoinciden ||
                        !identificadorCorrecto ||
                        idServidor == null ||
                        idServidor in idsUtilizados
                    ) {
                        pendientesRevision++

                        Log.w(
                            "StudyDeskSync",
                            "Entrega pendiente de revision. " +
                                    "Room: ${entrega.id}"
                        )
                        continue
                    }

                    if (!cuentaActiva(usuarioId, correo)) {
                        return@launch
                    }

                    val actualizados =
                        repository.guardarServidorId(
                            entregaId = entrega.id,
                            usuarioId = usuarioId,
                            servidorId = idServidor
                        )

                    if (actualizados == 1) {
                        idsUtilizados.add(idServidor)
                        vinculadas++

                    } else {
                        pendientesRevision++
                    }
                }

                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Entregas vinculadas: $vinculadas. " +
                                "Pendientes de revision: $pendientesRevision."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Sincronizacion incompleta. " +
                                "Vinculadas: $vinculadas. Revisa la conexion."
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
