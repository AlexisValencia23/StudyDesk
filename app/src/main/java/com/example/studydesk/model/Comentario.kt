package com.example.studydesk.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "comentarios")
data class Comentario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val archivoPdf: String,
    val autor: String,
    val texto: String,
    val fecha: String
)