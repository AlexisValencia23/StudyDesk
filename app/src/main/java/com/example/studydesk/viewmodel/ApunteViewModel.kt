
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

    // Usuario conectado
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    // Correo utilizado para el servidor
    private val correoActivo = MutableStateFlow<String?>(null)

    // Resultado del guardado en el servidor
    private val _mensajeSincronizacion =
        MutableStateFlow<String?>(null)

    val mensajeSincronizacion: StateFlow<String?> =
        _mensajeSincronizacion

    // Apuntes guardados en Room
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

    // Crear apunte local y enviarlo al servidor
    fun agregarApunte(
        materia: String,
        titulo: String,
        contenido: String
    ) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        // Validaciones
        if (
            Validaciones.validarMateriaApunte(materia) != null ||
            Validaciones.validarTituloApunte(titulo) != null ||
            Validaciones.validarContenidoApunte(contenido) != null
        ) {
            return
        }

        // Crear los datos una sola vez
        val nuevoApunte = Apunte(
            materia = materia.trim(),
            titulo = titulo.trim(),
            contenido = contenido.trim(),
            fecha = FechaUtil.hoy(),
            usuarioId = usuarioId
        )

        viewModelScope.launch {

            _mensajeSincronizacion.value = null

            // PASO 1: Guardar localmente en Room
            repository.agregarApunte(nuevoApunte)

            Log.d(
                "StudyDeskSync",
                "Apunte guardado correctamente en Room"
            )

            // Comprobar que tenemos correo
            if (correo == null) {

                _mensajeSincronizacion.value =
                    "Guardado en Room, pero falta el correo para enviarlo al servidor."

                return@launch
            }

            // PASO 2: Enviar una copia a Spring Boot
            try {

                val apunteServidor = ApiApunte(
                    materia = nuevoApunte.materia,
                    titulo = nuevoApunte.titulo,
                    contenido = nuevoApunte.contenido,
                    fecha = nuevoApunte.fecha,
                    correoUsuario = correo
                )

                apiRepository.crear(apunteServidor)

                _mensajeSincronizacion.value =
                    "Apunte guardado en Room y Spring Boot."

                Log.d(
                    "StudyDeskSync",
                    "Apunte guardado correctamente en Spring Boot"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _mensajeSincronizacion.value =
                    "El apunte se guardo en Room, pero no pudo enviarse al servidor."

                Log.e(
                    "StudyDeskSync",
                    "Error al guardar el apunte en Spring Boot",
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
