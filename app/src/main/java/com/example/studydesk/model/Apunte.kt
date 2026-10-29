
package com.example.studydesk.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "apuntes")
data class Apunte(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val materia: String,
    val titulo: String,
    val contenido: String,
    val fecha: String,

    // Identifica al dueño del apunte
    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0
)
