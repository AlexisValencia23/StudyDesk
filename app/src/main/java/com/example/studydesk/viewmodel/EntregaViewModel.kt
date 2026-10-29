
package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Entrega
import com.example.studydesk.repository.EntregaRepository
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntregaViewModel(
    private val repository: EntregaRepository
) : ViewModel() {

    // Lista de entregas almacenadas en Room.
    val entregas: StateFlow<List<Entrega>> =
        repository.obtenerTodas()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Crear una nueva entrega.
    fun agregarEntrega(
        materia: String,
        titulo: String,
        fecha: String
    ) {
        // Validar antes de guardar en Room.
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
                    fecha = fecha.trim()
                )
            )
        }
    }
}
