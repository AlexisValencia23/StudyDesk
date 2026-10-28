package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Apunte
import com.example.studydesk.repository.ApunteRepository
import com.example.studydesk.util.FechaUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApunteViewModel(private val repository: ApunteRepository) : ViewModel() {

    // Lista de apuntes que la pantalla observa; se actualiza sola al crear o editar uno
    val apuntes: StateFlow<List<Apunte>> = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarApunte(materia: String, titulo: String, contenido: String) {
        viewModelScope.launch {
            repository.agregarApunte(
                Apunte(
                    materia = materia.trim(),
                    titulo = titulo.trim(),
                    contenido = contenido.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }

    fun editarApunte(apunte: Apunte, nuevoTitulo: String, nuevoContenido: String) {
        viewModelScope.launch {
            repository.editarApunte(
                apunte.copy(
                    titulo = nuevoTitulo.trim(),
                    contenido = nuevoContenido.trim(),
                    fecha = FechaUtil.hoy()
                )
            )
        }
    }
}