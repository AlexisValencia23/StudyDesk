
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class EntregaViewModel(
    private val repository: EntregaRepository,
    private val apiRepository: EntregaApiRepository = EntregaApiRepository()
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

    // Mostrar entregas del usuario conectado
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

    // Establecer usuario local
    fun establecerUsuario(usuarioId: Int?) {

        if (usuarioActivo.value != usuarioId) {
            correoActivo.value = null
            _mensajeSincronizacion.value = null
        }

        usuarioActivo.value = usuarioId
    }

    // Establecer correo para Spring Boot
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

    // Crear una entrega en Room y Spring Boot
    fun agregarEntrega(
        materia: String,
        titulo: String,
        fecha: String
    ) {

        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        // Validar los campos
        if (
            Validaciones.validarMateriaEntrega(materia) != null ||
            Validaciones.validarTituloEntrega(titulo) != null ||
            Validaciones.validarFechaEntrega(fecha) != null
        ) {
            return
        }

        // Preparar la entrega local
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

            // Comprobar si existe un correo
            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Entrega guardada en Room, pero falta el correo del servidor."

                return@launch
            }

            // Evitar enviar si cambio la cuenta
            if (
                usuarioActivo.value != usuarioId ||
                correoActivo.value != correo
            ) {
                return@launch
            }

            // PASO 2: Crear entrega en Spring Boot
            try {

                val entregaServidor = ApiEntrega(
                    materia = nuevaEntrega.materia,
                    titulo = nuevaEntrega.titulo,
                    fecha = nuevaEntrega.fecha,
                    correoUsuario = correo
                )

                val respuesta = apiRepository.crear(entregaServidor)

                // PASO 3: Obtener identificador del servidor
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

                // PASO 4: Asociar los dos identificadores
                val actualizados = repository.guardarServidorId(
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
                        "No se pudo guardar servidorId de la entrega en Room"
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

    // Eliminar una entrega local
    // La sincronizacion de eliminacion se implementara despues
    fun eliminarEntrega(entrega: Entrega) {

        val usuarioId = usuarioActivo.value ?: return

        if (entrega.usuarioId != usuarioId) {
            return
        }

        viewModelScope.launch {

            repository.eliminarEntrega(
                entregaId = entrega.id,
                usuarioId = usuarioId
            )
        }
    }
}
