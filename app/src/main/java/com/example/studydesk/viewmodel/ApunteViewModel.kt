package com.example.studydesk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Apunte
import com.example.studydesk.network.ApiApunte
import com.example.studydesk.repository.ApunteApiRepository
import com.example.studydesk.repository.ApunteRepository
import com.example.studydesk.util.FechaUtil
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

class ApunteViewModel(
    private val repository: ApunteRepository,
    private val apiRepository: ApunteApiRepository = ApunteApiRepository(),
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
    val apuntes: StateFlow<List<Apunte>> = usuarioActivo
        .flatMapLatest { usuarioId ->
            if (usuarioId == null) {
                flowOf(emptyList<Apunte>())
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

    // CREAR APUNTE EN ROOM Y SPRING BOOT
    fun agregarApunte(
        materia: String,
        titulo: String,
        contenido: String
    ) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (
            Validaciones.validarMateriaApunte(materia) != null ||
            Validaciones.validarTituloApunte(titulo) != null ||
            Validaciones.validarContenidoApunte(contenido) != null
        ) {
            return
        }

        val nuevoApunte = Apunte(
            materia = materia.trim(),
            titulo = titulo.trim(),
            contenido = contenido.trim(),
            fecha = FechaUtil.hoy(),
            usuarioId = usuarioId
        )

        viewModelScope.launch {
            _mensajeSincronizacion.value = null

            val idLocal =
                repository.agregarApunte(nuevoApunte)

            val idCliente =
                identificadorSync.apunte(idLocal.toInt())


            if (correo == null) {
                _mensajeSincronizacion.value =
                    "Apunte guardado en Room, pero falta el correo."
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                val respuesta = apiRepository.crear(
                    ApiApunte(
                        materia = nuevoApunte.materia,
                        titulo = nuevoApunte.titulo,
                        contenido = nuevoApunte.contenido,
                        fecha = nuevoApunte.fecha,
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
                        "Respuesta no valida para apunte Room: $idLocal"
                    )
                    return@launch
                }

                if (!cuentaActiva(usuarioId, correo)) {
                    return@launch
                }

                val actualizados =
                    repository.guardarServidorId(
                        apunteId = idLocal.toInt(),
                        usuarioId = usuarioId,
                        servidorId = idServidor
                    )

                if (actualizados == 1) {
                    _mensajeSincronizacion.value =
                        "Apunte guardado y vinculado con Spring Boot."

                } else {
                    _mensajeSincronizacion.value =
                        "Apunte enviado, pero no se pudo vincular en Room."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Apunte guardado en Room. Sincronizacion pendiente."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al sincronizar el apunte",
                    e
                )
            }
        }
    }

    // EDITAR APUNTE
    fun editarApunte(
        apunte: Apunte,
        nuevoTitulo: String,
        nuevoContenido: String
    ) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (apunte.usuarioId != usuarioId) {
            return
        }

        if (
            Validaciones.validarTituloApunte(nuevoTitulo) != null ||
            Validaciones.validarContenidoApunte(nuevoContenido) != null
        ) {
            return
        }

        val apunteEditado = apunte.copy(
            titulo = nuevoTitulo.trim(),
            contenido = nuevoContenido.trim(),
            fecha = FechaUtil.hoy()
        )

        viewModelScope.launch {
            _mensajeSincronizacion.value = null

            repository.editarApunte(
                apunte = apunteEditado,
                usuarioId = usuarioId
            )


            val idServidor = apunte.servidorId

            if (idServidor == null) {
                _mensajeSincronizacion.value =
                    "Apunte editado en Room, pero no esta vinculado."
                return@launch
            }

            if (correo == null) {
                _mensajeSincronizacion.value =
                    "Apunte editado en Room, pero falta el correo."
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                val registroServidor =
                    apiRepository.obtenerPorId(idServidor)

                val correoServidor =
                    registroServidor.correoUsuario
                        ?.trim()
                        ?.lowercase(Locale.ROOT)

                if (correoServidor != correo) {
                    _mensajeSincronizacion.value =
                        "El registro remoto no coincide con la cuenta."
                    return@launch
                }

                if (!cuentaActiva(usuarioId, correo)) {
                    return@launch
                }

                val respuesta = apiRepository.editar(
                    id = idServidor,
                    apunte = ApiApunte(
                        materia = apunteEditado.materia,
                        titulo = apunteEditado.titulo,
                        contenido = apunteEditado.contenido,
                        fecha = apunteEditado.fecha,
                        correoUsuario = correo,
                        idCliente = registroServidor.idCliente
                    )
                )

                if (
                    respuesta.correoUsuario
                        ?.trim()
                        ?.lowercase(Locale.ROOT) != correo
                ) {
                    if (cuentaActiva(usuarioId, correo)) {
                        _mensajeSincronizacion.value =
                            "El servidor devolvio un correo diferente."
                    }
                    return@launch
                }

                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Apunte editado en Room y Spring Boot."
                }


            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Apunte editado en Room, pero fallo Spring Boot."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al editar el apunte",
                    e
                )
            }
        }
    }

    // ELIMINAR APUNTE
    fun eliminarApunte(apunte: Apunte) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (apunte.usuarioId != usuarioId) {
            return
        }

        viewModelScope.launch {
            _mensajeSincronizacion.value = null

            val idServidor = apunte.servidorId

            if (idServidor == null) {
                repository.eliminarApunte(
                    apunteId = apunte.id,
                    usuarioId = usuarioId
                )


                _mensajeSincronizacion.value =
                    "Apunte local eliminado."
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

                repository.eliminarApunte(
                    apunteId = apunte.id,
                    usuarioId = usuarioId
                )

                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Apunte eliminado de Room y Spring Boot."
                }


            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "No se completo la eliminacion."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al eliminar el apunte",
                    e
                )
            }
        }
    }

    // SINCRONIZAR APUNTES PENDIENTES
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
            var vinculados = 0
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
                            "No hay apuntes pendientes de sincronizacion."
                    }

                    return@launch
                }

                val remotos =
                    apiRepository.obtenerPorCorreo(correo)
                        .toMutableList()

                val idsUtilizados = locales
                    .mapNotNull { it.servidorId }
                    .toMutableSet()

                for (apunte in pendientes) {
                    if (!cuentaActiva(usuarioId, correo)) {
                        return@launch
                    }

                    val idCliente =
                        identificadorSync.apunte(apunte.id)

                    val porIdentificador = remotos.filter {
                        it.idCliente == idCliente
                    }

                    if (porIdentificador.size > 1) {
                        pendientesRevision++

                        Log.w(
                            "StudyDeskSync",
                            "Identificador de apunte duplicado: $idCliente"
                        )
                        continue
                    }

                    // Registros anteriores a idCliente
                    val antiguos = remotos.filter {
                        it.idCliente == null &&
                                it.materia == apunte.materia &&
                                it.titulo == apunte.titulo &&
                                it.contenido == apunte.contenido &&
                                it.fecha == apunte.fecha &&
                                it.correoUsuario
                                    ?.trim()
                                    ?.lowercase(Locale.ROOT) == correo &&
                                it.id !in idsUtilizados
                    }

                    val respuesta: ApiApunte
                    var registroAntiguo = false

                    if (porIdentificador.size == 1) {
                        // Ya existe en Spring Boot
                        respuesta = porIdentificador.first()

                    } else if (antiguos.size == 1) {
                        // Posible registro de la version anterior
                        respuesta = antiguos.first()
                        registroAntiguo = true

                    } else if (antiguos.size > 1) {
                        pendientesRevision++

                        Log.w(
                            "StudyDeskSync",
                            "Apunte antiguo ambiguo. Room: ${apunte.id}"
                        )
                        continue

                    } else {
                        // Crear o recuperar con el mismo ID cliente
                        respuesta = apiRepository.crear(
                            ApiApunte(
                                materia = apunte.materia,
                                titulo = apunte.titulo,
                                contenido = apunte.contenido,
                                fecha = apunte.fecha,
                                correoUsuario = correo,
                                idCliente = idCliente
                            )
                        )

                        remotos.add(respuesta)
                    }

                    val correoServidor = respuesta.correoUsuario
                        ?.trim()
                        ?.lowercase(Locale.ROOT)

                    val datosCoinciden =
                        respuesta.materia == apunte.materia &&
                                respuesta.titulo == apunte.titulo &&
                                respuesta.contenido == apunte.contenido &&
                                respuesta.fecha == apunte.fecha

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
                            "Apunte pendiente de revision. " +
                                    "Room: ${apunte.id}"
                        )
                        continue
                    }

                    if (!cuentaActiva(usuarioId, correo)) {
                        return@launch
                    }

                    val actualizados =
                        repository.guardarServidorId(
                            apunteId = apunte.id,
                            usuarioId = usuarioId,
                            servidorId = idServidor
                        )

                    if (actualizados == 1) {
                        idsUtilizados.add(idServidor)
                        vinculados++

                    } else {
                        pendientesRevision++
                    }
                }

                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Apuntes vinculados: $vinculados. " +
                                "Pendientes de revision: $pendientesRevision."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva(usuarioId, correo)) {
                    _mensajeSincronizacion.value =
                        "Sincronizacion incompleta. " +
                                "Vinculados: $vinculados. Revisa la conexion."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al sincronizar apuntes pendientes",
                    e
                )

            } finally {
                _sincronizandoPendientes.value = false
            }
        }
    }
}
