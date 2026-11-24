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

// Etiqueta para filtrar los errores de sincronización en Logcat
private const val ETIQUETA_SYNC = "StudyDeskSync"

// Resultado de intentar vincular un apunte pendiente con el servidor
private enum class ResultadoVinculo {
    VINCULADO,
    REVISION,
    CUENTA_CAMBIADA
}

// Deja el correo en minúsculas y sin espacios para poder compararlo
private fun normalizarCorreo(correo: String?): String? {
    return correo?.trim()?.lowercase(Locale.ROOT)
}

class ApunteViewModel(
    private val repository: ApunteRepository,
    private val apiRepository: ApunteApiRepository = ApunteApiRepository(),
    private val identificadorSync: IdentificadorSync
) : ViewModel() {

    private val usuarioActivo = MutableStateFlow<Int?>(null)
    private val correoActivo = MutableStateFlow<String?>(null)

    private val _mensajeSincronizacion = MutableStateFlow<String?>(null)
    val mensajeSincronizacion: StateFlow<String?> = _mensajeSincronizacion

    private val _sincronizandoPendientes = MutableStateFlow(false)
    val sincronizandoPendientes: StateFlow<Boolean> = _sincronizandoPendientes

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

    // CUENTA ACTIVA

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

        correoActivo.value = normalizarCorreo(correo)
            ?.takeIf { it.isNotEmpty() }
    }

    // Revisa que el usuario siga siendo el mismo mientras responde el servidor
    private fun cuentaActiva(usuarioId: Int, correo: String): Boolean {
        return usuarioActivo.value == usuarioId &&
                correoActivo.value == correo
    }

    private fun mostrarMensaje(texto: String?) {
        _mensajeSincronizacion.value = texto
    }

    // Muestra el mensaje solo si el usuario no cambió de cuenta
    private fun mostrarMensajeSiCuentaActiva(
        usuarioId: Int,
        correo: String,
        texto: String
    ) {
        if (cuentaActiva(usuarioId, correo)) {
            mostrarMensaje(texto)
        }
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
            mostrarMensaje(null)

            val idLocal = repository.agregarApunte(nuevoApunte).toInt()
            val idCliente = identificadorSync.apunte(idLocal)

            if (correo == null) {
                mostrarMensaje("Apunte guardado en Room, pero falta el correo.")
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                enviarApunteNuevo(
                    idLocal = idLocal,
                    apunte = nuevoApunte,
                    idCliente = idCliente,
                    usuarioId = usuarioId,
                    correo = correo
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarMensajeSiCuentaActiva(
                    usuarioId,
                    correo,
                    "Apunte guardado en Room. Sincronización pendiente."
                )
                Log.e(ETIQUETA_SYNC, "Error al sincronizar el apunte", e)
            }
        }
    }

    // Envía un apunte recién creado a Spring Boot y guarda su ID remoto
    private suspend fun enviarApunteNuevo(
        idLocal: Int,
        apunte: Apunte,
        idCliente: String,
        usuarioId: Int,
        correo: String
    ) {
        val respuesta = apiRepository.crear(
            ApiApunte(
                materia = apunte.materia,
                titulo = apunte.titulo,
                contenido = apunte.contenido,
                fecha = apunte.fecha,
                correoUsuario = correo,
                idCliente = idCliente
            )
        )

        val idServidor = respuesta.id

        if (
            idServidor == null ||
            respuesta.idCliente != idCliente ||
            normalizarCorreo(respuesta.correoUsuario) != correo
        ) {
            mostrarMensajeSiCuentaActiva(
                usuarioId,
                correo,
                "La respuesta del servidor necesita revisión."
            )
            Log.w(ETIQUETA_SYNC, "Respuesta no válida para apunte Room: $idLocal")
            return
        }

        if (!cuentaActiva(usuarioId, correo)) {
            return
        }

        val actualizados = repository.guardarServidorId(
            apunteId = idLocal,
            usuarioId = usuarioId,
            servidorId = idServidor
        )

        if (actualizados == 1) {
            mostrarMensaje("Apunte guardado y vinculado con Spring Boot.")
        } else {
            mostrarMensaje("Apunte enviado, pero no se pudo vincular en Room.")
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
            mostrarMensaje(null)

            repository.editarApunte(
                apunte = apunteEditado,
                usuarioId = usuarioId
            )

            val idServidor = apunte.servidorId

            if (idServidor == null) {
                mostrarMensaje("Apunte editado en Room, pero no está vinculado.")
                return@launch
            }

            if (correo == null) {
                mostrarMensaje("Apunte editado en Room, pero falta el correo.")
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                enviarEdicion(apunteEditado, idServidor, usuarioId, correo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarMensajeSiCuentaActiva(
                    usuarioId,
                    correo,
                    "Apunte editado en Room, pero falló Spring Boot."
                )
                Log.e(ETIQUETA_SYNC, "Error al editar el apunte", e)
            }
        }
    }

    // Envía los cambios de un apunte ya vinculado a Spring Boot
    private suspend fun enviarEdicion(
        apunteEditado: Apunte,
        idServidor: Long,
        usuarioId: Int,
        correo: String
    ) {
        val registroServidor = apiRepository.obtenerPorId(idServidor)

        if (normalizarCorreo(registroServidor.correoUsuario) != correo) {
            mostrarMensaje("El registro remoto no coincide con la cuenta.")
            return
        }

        if (!cuentaActiva(usuarioId, correo)) {
            return
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

        if (normalizarCorreo(respuesta.correoUsuario) != correo) {
            mostrarMensajeSiCuentaActiva(
                usuarioId,
                correo,
                "El servidor devolvió un correo diferente."
            )
            return
        }

        mostrarMensajeSiCuentaActiva(
            usuarioId,
            correo,
            "Apunte editado en Room y Spring Boot."
        )
    }

    // ELIMINAR APUNTE

    fun eliminarApunte(apunte: Apunte) {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (apunte.usuarioId != usuarioId) {
            return
        }

        viewModelScope.launch {
            mostrarMensaje(null)

            val idServidor = apunte.servidorId

            // Apunte que nunca se envió al servidor: solo se borra en Room
            if (idServidor == null) {
                repository.eliminarApunte(
                    apunteId = apunte.id,
                    usuarioId = usuarioId
                )
                mostrarMensaje("Apunte local eliminado.")
                return@launch
            }

            if (correo == null) {
                mostrarMensaje("No se pudo eliminar: falta el correo.")
                return@launch
            }

            if (!cuentaActiva(usuarioId, correo)) {
                return@launch
            }

            try {
                eliminarEnServidorYRoom(apunte, idServidor, usuarioId, correo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarMensajeSiCuentaActiva(
                    usuarioId,
                    correo,
                    "No se completó la eliminación."
                )
                Log.e(ETIQUETA_SYNC, "Error al eliminar el apunte", e)
            }
        }
    }

    // Busca un apunte en el servidor. Si ya no existe (404) devuelve null
    private suspend fun buscarApunteRemoto(idServidor: Long): ApiApunte? {
        return try {
            apiRepository.obtenerPorId(idServidor)
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    // Elimina el apunte en Spring Boot y después en Room
    private suspend fun eliminarEnServidorYRoom(
        apunte: Apunte,
        idServidor: Long,
        usuarioId: Int,
        correo: String
    ) {
        val registroServidor = buscarApunteRemoto(idServidor)

        if (
            registroServidor != null &&
            normalizarCorreo(registroServidor.correoUsuario) != correo
        ) {
            mostrarMensaje("No se eliminó: el registro pertenece a otro correo.")
            return
        }

        if (!cuentaActiva(usuarioId, correo)) {
            return
        }

        if (registroServidor != null) {
            val respuesta = apiRepository.eliminar(idServidor)

            if (!respuesta.isSuccessful && respuesta.code() != 404) {
                mostrarMensaje("No se pudo eliminar en Spring Boot.")
                return
            }
        }

        repository.eliminarApunte(
            apunteId = apunte.id,
            usuarioId = usuarioId
        )

        mostrarMensajeSiCuentaActiva(
            usuarioId,
            correo,
            "Apunte eliminado de Room y Spring Boot."
        )
    }

    // SINCRONIZAR APUNTES PENDIENTES

    fun sincronizarPendientes() {
        val usuarioId = usuarioActivo.value ?: return
        val correo = correoActivo.value

        if (correo == null) {
            mostrarMensaje("No se puede sincronizar: falta el correo.")
            return
        }

        if (_sincronizandoPendientes.value) {
            return
        }

        _sincronizandoPendientes.value = true

        viewModelScope.launch {
            var vinculados = 0
            var pendientesRevision = 0

            mostrarMensaje(null)

            try {
                val locales = repository.obtenerPorUsuario(usuarioId).first()
                val pendientes = repository.obtenerPendientesPorUsuario(usuarioId)

                if (pendientes.isEmpty()) {
                    mostrarMensajeSiCuentaActiva(
                        usuarioId,
                        correo,
                        "No hay apuntes pendientes de sincronización."
                    )
                    return@launch
                }

                val remotos = apiRepository.obtenerPorCorreo(correo)
                    .toMutableList()

                val idsUtilizados = locales
                    .mapNotNull { it.servidorId }
                    .toMutableSet()

                for (apunte in pendientes) {
                    val resultado = vincularPendiente(
                        apunte = apunte,
                        usuarioId = usuarioId,
                        correo = correo,
                        remotos = remotos,
                        idsUtilizados = idsUtilizados
                    )

                    when (resultado) {
                        ResultadoVinculo.VINCULADO -> vinculados++
                        ResultadoVinculo.REVISION -> pendientesRevision++
                        ResultadoVinculo.CUENTA_CAMBIADA -> return@launch
                    }
                }

                mostrarMensajeSiCuentaActiva(
                    usuarioId,
                    correo,
                    "Apuntes vinculados: $vinculados. " +
                            "Pendientes de revisión: $pendientesRevision."
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                mostrarMensajeSiCuentaActiva(
                    usuarioId,
                    correo,
                    "Sincronización incompleta. " +
                            "Vinculados: $vinculados. Revisa la conexión."
                )
                Log.e(ETIQUETA_SYNC, "Error al sincronizar apuntes pendientes", e)

            } finally {
                _sincronizandoPendientes.value = false
            }
        }
    }

    // Vincula un apunte pendiente de Room con su registro en Spring Boot
    private suspend fun vincularPendiente(
        apunte: Apunte,
        usuarioId: Int,
        correo: String,
        remotos: MutableList<ApiApunte>,
        idsUtilizados: MutableSet<Long>
    ): ResultadoVinculo {
        if (!cuentaActiva(usuarioId, correo)) {
            return ResultadoVinculo.CUENTA_CAMBIADA
        }

        val idCliente = identificadorSync.apunte(apunte.id)

        val porIdentificador = remotos.filter { it.idCliente == idCliente }

        if (porIdentificador.size > 1) {
            Log.w(ETIQUETA_SYNC, "Identificador de apunte duplicado: $idCliente")
            return ResultadoVinculo.REVISION
        }

        val antiguos = buscarRegistrosAntiguos(
            remotos = remotos,
            apunte = apunte,
            correo = correo,
            idsUtilizados = idsUtilizados
        )

        val respuesta: ApiApunte
        var registroAntiguo = false

        if (porIdentificador.size == 1) {
            // Ya existe en Spring Boot
            respuesta = porIdentificador.first()

        } else if (antiguos.size == 1) {
            // Posible registro de la versión anterior (sin idCliente)
            respuesta = antiguos.first()
            registroAntiguo = true

        } else if (antiguos.size > 1) {
            Log.w(ETIQUETA_SYNC, "Apunte antiguo ambiguo. Room: ${apunte.id}")
            return ResultadoVinculo.REVISION

        } else {
            // No existe: se crea (o se recupera con el mismo idCliente)
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

        val identificadorCorrecto = if (registroAntiguo) {
            respuesta.idCliente == null
        } else {
            respuesta.idCliente == idCliente
        }

        val idServidor = respuesta.id

        if (
            normalizarCorreo(respuesta.correoUsuario) != correo ||
            !mismosDatos(respuesta, apunte) ||
            !identificadorCorrecto ||
            idServidor == null ||
            idServidor in idsUtilizados
        ) {
            Log.w(ETIQUETA_SYNC, "Apunte pendiente de revisión. Room: ${apunte.id}")
            return ResultadoVinculo.REVISION
        }

        if (!cuentaActiva(usuarioId, correo)) {
            return ResultadoVinculo.CUENTA_CAMBIADA
        }

        val actualizados = repository.guardarServidorId(
            apunteId = apunte.id,
            usuarioId = usuarioId,
            servidorId = idServidor
        )

        if (actualizados != 1) {
            return ResultadoVinculo.REVISION
        }

        idsUtilizados.add(idServidor)
        return ResultadoVinculo.VINCULADO
    }

    // Registros del servidor creados antes de existir idCliente y que
    // tienen los mismos datos que el apunte local
    private fun buscarRegistrosAntiguos(
        remotos: List<ApiApunte>,
        apunte: Apunte,
        correo: String,
        idsUtilizados: Set<Long>
    ): List<ApiApunte> {
        return remotos.filter {
            it.idCliente == null &&
                    mismosDatos(it, apunte) &&
                    normalizarCorreo(it.correoUsuario) == correo &&
                    it.id !in idsUtilizados
        }
    }

    // Compara el contenido de un registro del servidor con el apunte local
    private fun mismosDatos(remoto: ApiApunte, apunte: Apunte): Boolean {
        return remoto.materia == apunte.materia &&
                remoto.titulo == apunte.titulo &&
                remoto.contenido == apunte.contenido &&
                remoto.fecha == apunte.fecha
    }
}