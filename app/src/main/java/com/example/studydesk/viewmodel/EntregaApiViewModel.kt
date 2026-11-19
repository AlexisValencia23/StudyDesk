
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
import retrofit2.HttpException
import java.util.Locale

class EntregaApiViewModel(
    private val repository: EntregaApiRepository = EntregaApiRepository()
) : ViewModel() {

    // Cuenta que consulta el servidor
    private data class CuentaActiva(
        val usuarioId: Int? = null,
        val correo: String? = null
    )

    private var cuentaActiva: CuentaActiva? = null

    // Entregas obtenidas desde Spring Boot
    private val _entregas =
        MutableStateFlow<List<ApiEntrega>>(emptyList())

    val entregas: StateFlow<List<ApiEntrega>> = _entregas

    // Estado de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Mensajes de error
    private val _mensajeError =
        MutableStateFlow<String?>(null)

    val mensajeError: StateFlow<String?> = _mensajeError

    // Metodo antiguo: consultar por ID local
    fun establecerUsuario(usuarioId: Int?) {
        actualizarCuenta(
            usuarioId?.let {
                CuentaActiva(usuarioId = it)
            }
        )
    }

    // Metodo nuevo: consultar por correo
    fun establecerCorreo(correo: String?) {
        val correoNormalizado = correo
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }

        actualizarCuenta(
            correoNormalizado?.let {
                CuentaActiva(correo = it)
            }
        )
    }

    // Cambiar la cuenta activa
    private fun actualizarCuenta(cuenta: CuentaActiva?) {
        if (cuentaActiva == cuenta) return

        cuentaActiva = cuenta

        _entregas.value = emptyList()
        _mensajeError.value = null
        _cargando.value = false

        if (cuenta != null) {
            cargarEntregas()
        }
    }

    // GET: Consultar las entregas del servidor
    fun cargarEntregas() {
        val cuenta = cuentaActiva ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                val resultado = consultarEntregas(cuenta)

                if (cuentaActiva == cuenta) {
                    _entregas.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva == cuenta) {
                    _mensajeError.value =
                        "No se pudieron cargar las entregas del servidor."
                }

            } finally {
                if (cuentaActiva == cuenta) {
                    _cargando.value = false
                }
            }
        }
    }

    // Consultar por correo o por ID
    private suspend fun consultarEntregas(
        cuenta: CuentaActiva
    ): List<ApiEntrega> {

        return when {
            cuenta.correo != null -> {
                repository.obtenerPorCorreo(cuenta.correo)
            }

            cuenta.usuarioId != null -> {
                repository.obtenerPorUsuario(cuenta.usuarioId)
            }

            else -> emptyList()
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

        ejecutarOperacion { cuenta ->
            repository.crear(
                ApiEntrega(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    fecha = fecha.trim(),
                    usuarioId = cuenta.usuarioId,
                    correoUsuario = cuenta.correo
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
        val cuenta = cuentaActiva ?: return
        val entregaId = entrega.id ?: return

        if (!perteneceALaCuenta(entrega, cuenta)) return

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
        val cuenta = cuentaActiva ?: return
        val entregaId = entrega.id ?: return

        if (!perteneceALaCuenta(entrega, cuenta)) return

        ejecutarOperacion {
            val respuesta = repository.eliminar(entregaId)

            if (!respuesta.isSuccessful) {
                throw HttpException(respuesta)
            }
        }
    }

    // Comprobar localmente el propietario
    private fun perteneceALaCuenta(
        entrega: ApiEntrega,
        cuenta: CuentaActiva
    ): Boolean {

        return when {
            cuenta.correo != null -> {
                entrega.correoUsuario
                    .orEmpty()
                    .trim()
                    .equals(cuenta.correo, ignoreCase = true)
            }

            cuenta.usuarioId != null -> {
                entrega.usuarioId == cuenta.usuarioId
            }

            else -> false
        }
    }

    // Ejecutar operaciones y actualizar la lista
    private fun ejecutarOperacion(
        operacion: suspend (CuentaActiva) -> Unit
    ) {
        val cuenta = cuentaActiva ?: return

        viewModelScope.launch {
            if (cuentaActiva != cuenta) return@launch

            _cargando.value = true
            _mensajeError.value = null

            try {
                operacion(cuenta)

                val resultado = consultarEntregas(cuenta)

                if (cuentaActiva == cuenta) {
                    _entregas.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva == cuenta) {
                    _mensajeError.value =
                        "No se pudo completar la operacion en el servidor."
                }

            } finally {
                if (cuentaActiva == cuenta) {
                    _cargando.value = false
                }
            }
        }
    }
}
