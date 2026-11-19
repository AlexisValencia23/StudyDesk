
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.network.ApiEntrega
import com.example.studydesk.repository.EntregaApiRepository
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EntregaApiViewModel(
    private val repository: EntregaApiRepository = EntregaApiRepository()
) : ViewModel() {

    // Entregas obtenidas desde Spring Boot
    private val _entregas =
        MutableStateFlow<List<ApiEntrega>>(emptyList())

    val entregas: StateFlow<List<ApiEntrega>> = _entregas

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

        _entregas.value = emptyList()
        _mensajeError.value = null
        _cargando.value = false

        if (usuarioId != null) {
            cargarEntregas()
        }
    }

    // GET: Obtener entregas del servidor
    fun cargarEntregas() {
        val usuarioId = usuarioActivo ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                val resultado =
                    repository.obtenerPorUsuario(usuarioId)

                if (usuarioActivo == usuarioId) {
                    _entregas.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (usuarioActivo == usuarioId) {
                    _mensajeError.value =
                        "No se pudieron cargar las entregas del servidor."
                }

            } finally {
                if (usuarioActivo == usuarioId) {
                    _cargando.value = false
                }
            }
        }
    }

    // POST: Crear una entrega
    fun agregarEntrega(
        materia: String,
        titulo: String,
        fecha: String
    ) {
        if (
            Validaciones.validarMateriaEntrega(materia) != null ||
            Validaciones.validarTituloEntrega(titulo) != null ||
            Validaciones.validarFechaEntrega(fecha) != null
        ) {
            _mensajeError.value = "Revisa los datos de la entrega."
            return
        }

        ejecutarOperacion { usuarioId ->
            repository.crear(
                ApiEntrega(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    fecha = fecha.trim(),
                    usuarioId = usuarioId
                )
            )
        }
    }

    // PUT: Editar una entrega
    fun editarEntrega(
        entrega: ApiEntrega,
        nuevaMateria: String,
        nuevoTitulo: String,
        nuevaFecha: String
    ) {
        val usuarioId = usuarioActivo ?: return
        val entregaId = entrega.id ?: return

        if (entrega.usuarioId != usuarioId) return

        if (
            Validaciones.validarMateriaEntrega(nuevaMateria) != null ||
            Validaciones.validarTituloEntrega(nuevoTitulo) != null ||
            Validaciones.validarFechaEntrega(nuevaFecha) != null
        ) {
            _mensajeError.value = "Revisa los datos de la entrega."
            return
        }

        ejecutarOperacion {
            repository.editar(
                id = entregaId,
                entrega = entrega.copy(
                    materia = nuevaMateria.trim(),
                    titulo = nuevoTitulo.trim(),
                    fecha = nuevaFecha.trim()
                )
            )
        }
    }

    // DELETE: Eliminar una entrega
    fun eliminarEntrega(entrega: ApiEntrega) {
        val usuarioId = usuarioActivo ?: return
        val entregaId = entrega.id ?: return

        if (entrega.usuarioId != usuarioId) return

        ejecutarOperacion {
            val respuesta = repository.eliminar(entregaId)

            if (!respuesta.isSuccessful) {
                throw retrofit2.HttpException(respuesta)
            }
        }
    }

    // Ejecutar operaciones y actualizar entregas
    private fun ejecutarOperacion(
        operacion: suspend (Int) -> Unit
    ) {
        val usuarioId = usuarioActivo ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                operacion(usuarioId)

                val resultado =
                    repository.obtenerPorUsuario(usuarioId)

                if (usuarioActivo == usuarioId) {
                    _entregas.value = resultado
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
