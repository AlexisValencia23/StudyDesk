
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

    // Usuario conectado en Room
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    // Correo utilizado para Spring Boot
    private val correoActivo = MutableStateFlow<String?>(null)

    // Resultado de la sincronizacion
    private val _mensajeSincronizacion =
        MutableStateFlow<String?>(null)

    val mensajeSincronizacion: StateFlow<String?> =
        _mensajeSincronizacion

    // Mostrar apuntes del usuario conectado
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

    // Establecer usuario local
    fun establecerUsuario(usuarioId: Int?) {

        if (usuarioActivo.value != usuarioId) {
            correoActivo.value = null
            _mensajeSincronizacion.value = null
        }

        usuarioActivo.value = usuarioId
    }

    // Establecer correo del servidor
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

    // Crear apunte en Room y Spring Boot
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

            // PASO 1: Guardar en Room
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

            // PASO 2: Enviar a Spring Boot
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

                // PASO 3: Vincular identificadores
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

    // Editar apunte en Room y Spring Boot
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

            // PASO 1: Actualizar Room
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

                Log.w(
                    "StudyDeskSync",
                    "No se puede sincronizar la edicion: servidorId es null"
                )

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

            // PASO 2: Actualizar Spring Boot
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

    // Eliminar apunte de Room y Spring Boot
    fun eliminarApunte(apunte: Apunte) {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (apunte.usuarioId != usuarioId) return

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            val idServidor = apunte.servidorId

            // Si nunca se vinculo, eliminar solo de Room
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

            // No eliminar el registro local si falta el correo
            if (correo == null) {

                _mensajeSincronizacion.value =
                    "No se pudo eliminar: falta el correo del usuario."

                Log.w(
                    "StudyDeskSync",
                    "Eliminacion cancelada: correo no disponible"
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

                // PASO 1: Comprobar el registro remoto
                val registroServidor = try {
                    apiRepository.obtenerPorId(idServidor)
                } catch (e: HttpException) {

                    // Si ya no existe en Spring Boot, limpiar Room
                    if (e.code() == 404) {
                        null
                    } else {
                        throw e
                    }
                }

                if (registroServidor != null) {

                    // Verificar que el correo sea el esperado
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

                // PASO 2: Eliminar del servidor si existe
                if (registroServidor != null) {

                    val respuesta = apiRepository.eliminar(idServidor)

                    if (
                        !respuesta.isSuccessful &&
                        respuesta.code() != 404
                    ) {

                        _mensajeSincronizacion.value =
                            "No se pudo eliminar en Spring Boot. Se conservo en Room."

                        Log.w(
                            "StudyDeskSync",
                            "Error HTTP al eliminar apunte: ${respuesta.code()}"
                        )

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

                // PASO 3: Eliminar de Room
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
}
