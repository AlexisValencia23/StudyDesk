
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

        // Validar los campos
        if (
            Validaciones.validarMateriaApunte(materia) != null ||
            Validaciones.validarTituloApunte(titulo) != null ||
            Validaciones.validarContenidoApunte(contenido) != null
        ) {
            return
        }

        // Preparar el apunte local
        val nuevoApunte = Apunte(
            materia = materia.trim(),
            titulo = titulo.trim(),
            contenido = contenido.trim(),
            fecha = FechaUtil.hoy(),
            usuarioId = usuarioId
        )

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            // PASO 1: Guardar en Room y obtener el ID local
            val idLocal = repository.agregarApunte(nuevoApunte)

            Log.d(
                "StudyDeskSync",
                "Apunte guardado en Room. ID local: $idLocal"
            )

            // Comprobar que existe un correo
            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Apunte guardado en Room, pero falta el correo del servidor."

                return@launch
            }

            // Evitar enviar datos si cambio la cuenta
            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            // PASO 2: Crear el apunte en Spring Boot
            try {

                val apunteServidor = ApiApunte(
                    materia = nuevoApunte.materia,
                    titulo = nuevoApunte.titulo,
                    contenido = nuevoApunte.contenido,
                    fecha = nuevoApunte.fecha,
                    correoUsuario = correo
                )

                val respuesta = apiRepository.crear(apunteServidor)

                // PASO 3: Recuperar el ID del servidor
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

                // PASO 4: Asociar ambos identificadores
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

    // Editar apunte local
    fun editarApunte(
        apunte: Apunte,
        nuevoTitulo: String,
        nuevoContenido: String
    ) {

        val usuarioId = usuarioActivo.value ?: return

        if (apunte.usuarioId != usuarioId) return

        if (
            Validaciones.validarTituloApunte(nuevoTitulo) != null ||
            Validaciones.validarContenidoApunte(nuevoContenido) != null
        ) {
            return
        }

        viewModelScope.launch {

            repository.editarApunte(
                apunte.copy(
                    titulo = nuevoTitulo.trim(),
                    contenido = nuevoContenido.trim(),
                    fecha = FechaUtil.hoy()
                ),
                usuarioId
            )
        }
    }

    // Eliminar apunte local
    fun eliminarApunte(apunte: Apunte) {

        val usuarioId = usuarioActivo.value ?: return

        if (apunte.usuarioId != usuarioId) return

        viewModelScope.launch {

            repository.eliminarApunte(
                apunteId = apunte.id,
                usuarioId = usuarioId
            )
        }
    }
}
