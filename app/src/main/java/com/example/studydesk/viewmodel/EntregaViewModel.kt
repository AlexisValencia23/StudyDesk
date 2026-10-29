
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Entrega
import com.example.studydesk.repository.EntregaRepository
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntregaViewModel(
    private val repository: EntregaRepository
) : ViewModel() {

    // Identificador de la cuenta activa
    private val usuarioActivo = MutableStateFlow<Int?>(null)

    // Mostrar únicamente las entregas del usuario conectado
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

    // Cambiar el usuario conectado
    fun establecerUsuario(usuarioId: Int?) {
        usuarioActivo.value = usuarioId
    }

    // Crear una entrega
    fun agregarEntrega(
        materia: String,
        titulo: String,
        fecha: String
    ) {
        val usuarioId = usuarioActivo.value ?: return

        // Mantener las validaciones existentes
        if (
            Validaciones.validarMateriaEntrega(materia) != null ||
            Validaciones.validarTituloEntrega(titulo) != null ||
            Validaciones.validarFechaEntrega(fecha) != null
        ) {
            return
        }

        viewModelScope.launch {
            repository.agregarEntrega(
                Entrega(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    fecha = fecha.trim(),
                    usuarioId = usuarioId
                )
            )
        }
    }

    // Eliminar solamente una entrega propia
    fun eliminarEntrega(entrega: Entrega) {
        val usuarioId = usuarioActivo.value ?: return

        // Comprobar que la entrega pertenece al usuario
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
