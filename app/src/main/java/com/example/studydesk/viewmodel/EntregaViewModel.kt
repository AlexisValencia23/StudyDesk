package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Entrega
import com.example.studydesk.repository.EntregaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntregaViewModel(private val repository: EntregaRepository) : ViewModel() {

    // Lista de entregas que la pantalla observa; se actualiza sola al agregar una
    val entregas: StateFlow<List<Entrega>> = repository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarEntrega(materia: String, titulo: String, fecha: String) {
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