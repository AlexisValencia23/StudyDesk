
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Apunte
import com.example.studydesk.repository.ApunteRepository
import com.example.studydesk.util.FechaUtil
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApunteViewModel(
    private val repository: ApunteRepository
) : ViewModel() {

    // ID de la cuenta que está utilizando StudyDesk
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    // Mostrar únicamente los apuntes del usuario conectado
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

    // Cambiar la cuenta activa
    fun establecerUsuario(usuarioId: Int?) {
        usuarioActivo.value = usuarioId
    }

    // Crear un apunte
    fun agregarApunte(
        materia: String,
        titulo: String,
        contenido: String
    ) {
        val usuarioId = usuarioActivo.value ?: return

        if (
            Validaciones.validarMateriaApunte(materia) != null ||
            Validaciones.validarTituloApunte(titulo) != null ||
            Validaciones.validarContenidoApunte(contenido) != null
        ) {
            return
        }

        viewModelScope.launch {
            repository.agregarApunte(
                Apunte(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    contenido = contenido.trim(),
                    fecha = FechaUtil.hoy(),
                    usuarioId = usuarioId
                )
            )
        }
    }

    // Editar un apunte propio
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

    // Eliminar un apunte propio
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
