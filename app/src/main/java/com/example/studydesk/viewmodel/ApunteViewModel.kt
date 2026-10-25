package com.example.studydesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studydesk.model.Apunte
import com.example.studydesk.repository.ApunteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    fecha = fechaDeHoy()
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
                    fecha = fechaDeHoy()
                )
            )
        }
    }

    // La fecha se pone sola, así el usuario no tiene que escribirla (ej: "27 oct")
    private fun fechaDeHoy(): String {
        return SimpleDateFormat("d MMM", Locale("es", "CL")).format(Date())
    }
}