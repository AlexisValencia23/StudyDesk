
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

    // Identificador del usuario en Room
    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0,

    // Identificador del apunte en Spring Boot
    // Es null cuando todavía no se ha guardado en el servidor
    val servidorId: Long? = null
)
