
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
import retrofit2.HttpException
import java.util.Locale

class ApunteApiViewModel(
    private val repository: ApunteApiRepository = ApunteApiRepository()
) : ViewModel() {

    // Identificacion de la cuenta conectada
    private data class CuentaActiva(
        val usuarioId: Int? = null,
        val correo: String? = null
    )

    private var cuentaActiva: CuentaActiva? = null

    // Apuntes del servidor
    private val _apuntes =
        MutableStateFlow<List<ApiApunte>>(emptyList())

    val apuntes: StateFlow<List<ApiApunte>> = _apuntes

    // Estado de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // Mensajes de error
    private val _mensajeError =
        MutableStateFlow<String?>(null)

    val mensajeError: StateFlow<String?> = _mensajeError

    // Identificacion antigua por ID local
    fun establecerUsuario(usuarioId: Int?) {
        actualizarCuenta(
            usuarioId?.let {
                CuentaActiva(usuarioId = it)
            }
        )
    }

    // Nueva identificacion por correo
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

    // Cambiar cuenta activa
    private fun actualizarCuenta(cuenta: CuentaActiva?) {
        if (cuentaActiva == cuenta) return

        cuentaActiva = cuenta

        _apuntes.value = emptyList()
        _mensajeError.value = null
        _cargando.value = false

        if (cuenta != null) {
            cargarApuntes()
        }
    }

    // GET: Consultar apuntes del servidor
    fun cargarApuntes() {
        val cuenta = cuentaActiva ?: return

        viewModelScope.launch {
            _cargando.value = true
            _mensajeError.value = null

            try {
                val resultado = consultarApuntes(cuenta)

                if (cuentaActiva == cuenta) {
                    _apuntes.value = resultado
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                if (cuentaActiva == cuenta) {
                    _mensajeError.value =
                        "No se pudieron cargar los apuntes del servidor."
                }

            } finally {
                if (cuentaActiva == cuenta) {
                    _cargando.value = false
                }
            }
        }
    }

    // Consultar por correo o ID local
    private suspend fun consultarApuntes(
        cuenta: CuentaActiva
    ): List<ApiApunte> {

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

    // POST: Crear apunte
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

        ejecutarOperacion { cuenta ->
            repository.crear(
                ApiApunte(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    contenido = contenido.trim(),
                    fecha = FechaUtil.hoy(),
                    usuarioId = cuenta.usuarioId,
                    correoUsuario = cuenta.correo
                )
            )
        }
    }

    // PUT: Editar apunte
    fun editarApunte(
        apunte: ApiApunte,
        nuevoTitulo: String,
        nuevoContenido: String
    ) {
        val cuenta = cuentaActiva ?: return
        val apunteId = apunte.id ?: return

        if (!perteneceALaCuenta(apunte, cuenta)) return

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

    // DELETE: Eliminar apunte
    fun eliminarApunte(apunte: ApiApunte) {
        val cuenta = cuentaActiva ?: return
        val apunteId = apunte.id ?: return

        if (!perteneceALaCuenta(apunte, cuenta)) return

        ejecutarOperacion {
            val respuesta = repository.eliminar(apunteId)

            if (!respuesta.isSuccessful) {
                throw HttpException(respuesta)
            }
        }
    }

    // Verificacion local del propietario
    private fun perteneceALaCuenta(
        apunte: ApiApunte,
        cuenta: CuentaActiva
    ): Boolean {

        return when {
            cuenta.correo != null -> {
                apunte.correoUsuario
                    .orEmpty()
                    .trim()
                    .equals(cuenta.correo, ignoreCase = true)
            }

            cuenta.usuarioId != null -> {
                apunte.usuarioId == cuenta.usuarioId
            }

            else -> false
        }
    }

    // Ejecutar operaciones y actualizar apuntes
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

                val resultado = consultarApuntes(cuenta)

                if (cuentaActiva == cuenta) {
                    _apuntes.value = resultado
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
