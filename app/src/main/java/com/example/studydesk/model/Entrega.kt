
package com.example.studydesk.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "entregas")
data class Entrega(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val materia: String,
    val titulo: String,
    val fecha: String,

    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0
)
