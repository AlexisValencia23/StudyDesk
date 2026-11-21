
package com.example.studydesk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Apunte
import com.example.studydesk.network.ApiApunte
import com.example.studydesk.repository.ApunteApiRepository
import com.example.studydesk.repository.ApunteRepository
import com.example.studydesk.util.FechaUtil
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
    private val apiRepository: ApunteApiRepository = ApunteApiRepository()
) : ViewModel() {

    private val usuarioActivo = MutableStateFlow<Int?>(null)
    private val correoActivo = MutableStateFlow<String?>(null)

    private val _mensajeSincronizacion =
        MutableStateFlow<String?>(null)

    val mensajeSincronizacion: StateFlow<String?> =
        _mensajeSincronizacion

    // Evitar dos sincronizaciones manuales simultaneas
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

    // CREAR APUNTE
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

            val idLocal = repository.agregarApunte(nuevoApunte)

            Log.d(
                "StudyDeskSync",
                "Apunte guardado en Room. ID local: $idLocal"
            )

            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Apunte guardado en Room, pero falta el correo del servidor."

                return@launch
            }

            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            try {

                val apunteServidor = ApiApunte(
                    materia = nuevoApunte.materia,
                    titulo = nuevoApunte.titulo,
                    contenido = nuevoApunte.contenido,
                    fecha = nuevoApunte.fecha,
                    correoUsuario = correo
                )

                val respuesta = apiRepository.crear(apunteServidor)
                val idServidor = respuesta.id

                if (idServidor == null) {

                    _mensajeSincronizacion.value =
                        "El servidor recibio el apunte, pero no devolvio su ID."

                    Log.w(
                        "StudyDeskSync",
                        "Spring Boot no devolvio el ID del apunte"
                    )

                    return@launch
                }

                val actualizados = repository.guardarServidorId(
                    apunteId = idLocal.toInt(),
                    usuarioId = usuarioId,
                    servidorId = idServidor
                )

                if (actualizados == 1) {

                    if (
                        usuarioActivo.value == usuarioId &&
                        correoActivo.value == correo
                    ) {
                        _mensajeSincronizacion.value =
                            "Apunte guardado y vinculado con Spring Boot."
                    }

                    Log.d(
                        "StudyDeskSync",
                        "Apunte vinculado. Room: $idLocal, " +
                                "Spring Boot: $idServidor"
                    )

                } else {

                    _mensajeSincronizacion.value =
                        "Apunte enviado al servidor, pero no se pudo vincular en Room."

                    Log.w(
                        "StudyDeskSync",
                        "No se pudo guardar servidorId en Room"
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
                        "El apunte se guardo en Room, pero fallo la sincronizacion."
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

        if (apunte.usuarioId != usuarioId) return

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

            Log.d(
                "StudyDeskSync",
                "Apunte editado correctamente en Room. ID: ${apunte.id}"
            )

            val idServidor = apunte.servidorId

            if (idServidor == null) {

                _mensajeSincronizacion.value =
                    "Apunte editado en Room, pero no tiene ID del servidor."

                return@launch
            }

            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Apunte editado en Room, pero falta el correo."

                return@launch
            }

            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            try {

                val registroServidor =
                    apiRepository.obtenerPorId(idServidor)

                val correoServidor = registroServidor.correoUsuario
                    ?.trim()
                    ?.lowercase(Locale.ROOT)

                if (correoServidor != correo) {

                    _mensajeSincronizacion.value =
                        "El registro remoto no coincide con la cuenta."

                    Log.w(
                        "StudyDeskSync",
                        "Edicion cancelada: correo del servidor no coincide"
                    )

                    return@launch
                }

                if (
                    usuarioActivo.value != usuarioId ||
                    correoActivo.value != correo
                ) {
                    return@launch
                }

                val datosActualizados = ApiApunte(
                    materia = apunteEditado.materia,
                    titulo = apunteEditado.titulo,
                    contenido = apunteEditado.contenido,
                    fecha = apunteEditado.fecha,
                    correoUsuario = correo
                )

                apiRepository.editar(
                    id = idServidor,
                    apunte = datosActualizados
                )

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Apunte editado en Room y Spring Boot."
                }

                Log.d(
                    "StudyDeskSync",
                    "Apunte editado en Spring Boot. ID: $idServidor"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Apunte editado en Room, pero fallo la actualizacion en Spring Boot."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al editar el apunte en Spring Boot",
                    e
                )
            }
        }
    }

    // ELIMINAR APUNTE
    fun eliminarApunte(apunte: Apunte) {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (apunte.usuarioId != usuarioId) return

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            val idServidor = apunte.servidorId

            if (idServidor == null) {

                repository.eliminarApunte(
                    apunteId = apunte.id,
                    usuarioId = usuarioId
                )

                Log.d(
                    "StudyDeskSync",
                    "Apunte sin servidorId eliminado de Room. ID: ${apunte.id}"
                )

                _mensajeSincronizacion.value =
                    "Apunte local eliminado."

                return@launch
            }

            if (correo == null) {

                _mensajeSincronizacion.value =
                    "No se pudo eliminar: falta el correo del usuario."

                return@launch
            }

            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
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
                            "No se elimino: el registro remoto pertenece a otro correo."

                        Log.w(
                            "StudyDeskSync",
                            "Eliminacion cancelada: correo no coincide"
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

                if (registroServidor != null) {

                    val respuesta =
                        apiRepository.eliminar(idServidor)

                    if (
                        !respuesta.isSuccessful &&
                        respuesta.code() != 404
                    ) {

                        _mensajeSincronizacion.value =
                            "No se pudo eliminar en Spring Boot. Se conservo en Room."

                        return@launch
                    }

                    Log.d(
                        "StudyDeskSync",
                        "Apunte eliminado en Spring Boot. ID: $idServidor"
                    )

                } else {

                    Log.w(
                        "StudyDeskSync",
                        "El apunte ya no existe en Spring Boot. ID: $idServidor"
                    )
                }

                repository.eliminarApunte(
                    apunteId = apunte.id,
                    usuarioId = usuarioId
                )

                Log.d(
                    "StudyDeskSync",
                    "Apunte eliminado en Room. ID: ${apunte.id}"
                )

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Apunte eliminado de Room y Spring Boot."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "No se completo la eliminacion. Revisa la conexion con Spring Boot."
                }

                Log.e(
                    "StudyDeskSync",
                    "Error al eliminar el apunte",
                    e
                )
            }
        }
    }

    // NUEVO: SINCRONIZAR APUNTES PENDIENTES
    fun sincronizarPendientes() {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (correo == null) {
            _mensajeSincronizacion.value =
                "No se puede sincronizar: falta el correo."
            return
        }

        if (_sincronizandoPendientes.value) return

        _sincronizandoPendientes.value = true

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            try {

                // Obtener registros locales y pendientes
                val locales =
                    repository.obtenerPorUsuario(usuarioId).first()

                val pendientes =
                    repository.obtenerPendientesPorUsuario(usuarioId)

                if (pendientes.isEmpty()) {

                    _mensajeSincronizacion.value =
                        "No hay apuntes pendientes de sincronizacion."

                    Log.d(
                        "StudyDeskSync",
                        "No hay apuntes pendientes"
                    )

                    return@launch
                }

                // Consultar primero el servidor
                val remotos =
                    apiRepository.obtenerPorCorreo(correo)
                        .toMutableList()

                // IDs del servidor ya vinculados en Room
                val idsUtilizados = locales
                    .mapNotNull { it.servidorId }
                    .toMutableSet()

                var vinculados = 0
                var omitidos = 0

                for (apunte in pendientes) {

                    // Detener si cambio la cuenta
                    if (
                        usuarioActivo.value != usuarioId ||
                        correoActivo.value != correo
                    ) {
                        return@launch
                    }

                    // Buscar coincidencias exactas
                    val coincidencias = remotos.filter {
                        it.materia == apunte.materia &&
                                it.titulo == apunte.titulo &&
                                it.contenido == apunte.contenido &&
                                it.fecha == apunte.fecha &&
                                it.correoUsuario
                                    ?.trim()
                                    ?.lowercase(Locale.ROOT) == correo
                    }

                    // Detectar posibles coincidencias parciales
                    val similares = remotos.filter {
                        it.materia == apunte.materia &&
                                it.titulo == apunte.titulo
                    }

                    val idExistente = coincidencias
                        .singleOrNull()
                        ?.id

                    val idServidor = when {

                        // Ya existe una copia unica en el servidor
                        idExistente != null &&
                                idExistente !in idsUtilizados -> {

                            Log.d(
                                "StudyDeskSync",
                                "Apunte encontrado en servidor. " +
                                        "Room: ${apunte.id}, " +
                                        "Spring Boot: $idExistente"
                            )

                            idExistente
                        }

                        // Coincidencia ambigua: evitar otra copia
                        coincidencias.isNotEmpty() ||
                                similares.isNotEmpty() -> {

                            omitidos++

                            Log.w(
                                "StudyDeskSync",
                                "Apunte pendiente de revision: ${apunte.id}"
                            )

                            continue
                        }

                        // No existe una coincidencia: crear
                        else -> {

                            val respuesta = apiRepository.crear(
                                ApiApunte(
                                    materia = apunte.materia,
                                    titulo = apunte.titulo,
                                    contenido = apunte.contenido,
                                    fecha = apunte.fecha,
                                    correoUsuario = correo
                                )
                            )

                            remotos.add(respuesta)

                            respuesta.id
                        }
                    }

                    if (idServidor == null) {

                        omitidos++

                        Log.w(
                            "StudyDeskSync",
                            "No se recibio ID para apunte ${apunte.id}"
                        )

                        continue
                    }

                    // Guardar el ID remoto en Room
                    val actualizados =
                        repository.guardarServidorId(
                            apunteId = apunte.id,
                            usuarioId = usuarioId,
                            servidorId = idServidor
                        )

                    if (actualizados == 1) {

                        idsUtilizados.add(idServidor)
                        vinculados++

                        Log.d(
                            "StudyDeskSync",
                            "Pendiente vinculado. " +
                                    "Room: ${apunte.id}, " +
                                    "Spring Boot: $idServidor"
                        )

                    } else {

                        omitidos++

                        Log.w(
                            "StudyDeskSync",
                            "No se pudo vincular apunte ${apunte.id}"
                        )
                    }
                }

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "Apuntes vinculados: $vinculados. " +
                                "Pendientes de revision: $omitidos."
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                if (
                    usuarioActivo.value == usuarioId &&
                    correoActivo.value == correo
                ) {
                    _mensajeSincronizacion.value =
                        "No se completo la sincronizacion. Revisa la conexion."
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
