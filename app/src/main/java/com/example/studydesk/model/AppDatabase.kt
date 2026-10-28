package com.example.studydesk.model

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [Usuario::class, Entrega::class, Apunte::class, Comentario::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun entregaDao(): EntregaDao
    abstract fun apunteDao(): ApunteDao
    abstract fun comentarioDao(): ComentarioDao
}