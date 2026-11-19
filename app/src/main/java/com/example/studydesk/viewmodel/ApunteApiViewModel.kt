
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.network.ApiApunte
import com.example.studydesk.repository.ApunteApiRepository
import com.example.studydesk.util.FechaUtil
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApunteApiViewModel(
    private val repository: ApunteApiRepository = ApunteApiRepository()
) : ViewModel() {

    // Apuntes obtenidos desde Spring Boot
    private val _apuntes = MutableStateFlow<List<ApiApunte>>(emptyList())
    val apuntes: StateFlow<List<ApiApunte>> = _apuntes

    // Estado de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Mensajes de error
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError

    // Usuario conectado
    private var usuarioActivo: Int? = null

    // Establecer el usuario actual
    fun establecerUsuario(usuarioId: Int?) {
        usuarioActivo = usuarioId

        _apuntes.value = emptyList()
        _mensajeError.value = null
        _cargando.value = false

        if (usuarioId != null) {
            cargarApuntes()
        }
    }

    // GET: Obtener apuntes del servidor
    fun cargarApuntes() {
        val usuarioId = usuarioActivo ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                val resultado = repository.obtenerPorUsuario(usuarioId)

                if (usuarioActivo == usuarioId) {
                    _apuntes.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (usuarioActivo == usuarioId) {
                    _mensajeError.value =
                        "No se pudieron cargar los apuntes del servidor."
                }

            } finally {
                if (usuarioActivo == usuarioId) {
                    _cargando.value = false
                }
            }
        }
    }

    // POST: Crear un apunte en Spring Boot
    fun agregarApunte(
        materia: String,
        titulo: String,
        contenido: String
    ) {
        if (
            Validaciones.validarMateriaApunte(materia) != null ||
            Validaciones.validarTituloApunte(titulo) != null ||
            Validaciones.validarContenidoApunte(contenido) != null
        ) {
            _mensajeError.value = "Revisa los datos del apunte."
            return
        }

        ejecutarOperacion { usuarioId ->
            repository.crear(
                ApiApunte(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    contenido = contenido.trim(),
                    fecha = FechaUtil.hoy(),
                    usuarioId = usuarioId
                )
            )
        }
    }

    // PUT: Editar un apunte del servidor
    fun editarApunte(
        apunte: ApiApunte,
        nuevoTitulo: String,
        nuevoContenido: String
    ) {
        val usuarioId = usuarioActivo ?: return
        val apunteId = apunte.id ?: return

        if (apunte.usuarioId != usuarioId) return

        if (
            Validaciones.validarTituloApunte(nuevoTitulo) != null ||
            Validaciones.validarContenidoApunte(nuevoContenido) != null
        ) {
            _mensajeError.value = "Revisa los datos del apunte."
            return
        }

        ejecutarOperacion {
            repository.editar(
                id = apunteId,
                apunte = apunte.copy(
                    titulo = nuevoTitulo.trim(),
                    contenido = nuevoContenido.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }

    // DELETE: Eliminar un apunte del servidor
    fun eliminarApunte(apunte: ApiApunte) {
        val usuarioId = usuarioActivo ?: return
        val apunteId = apunte.id ?: return

        if (apunte.usuarioId != usuarioId) return

        ejecutarOperacion {
            repository.eliminar(apunteId)
        }
    }

    // Ejecutar operaciones y actualizar la lista
    private fun ejecutarOperacion(
        operacion: suspend (Int) -> Unit
    ) {
        val usuarioId = usuarioActivo ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                operacion(usuarioId)

                val resultado = repository.obtenerPorUsuario(usuarioId)

                if (usuarioActivo == usuarioId) {
                    _apuntes.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (usuarioActivo == usuarioId) {
                    _mensajeError.value =
                        "No se pudo completar la operación en el servidor."
                }

            } finally {
                if (usuarioActivo == usuarioId) {
                    _cargando.value = false
                }
            }
        }
    }
}
