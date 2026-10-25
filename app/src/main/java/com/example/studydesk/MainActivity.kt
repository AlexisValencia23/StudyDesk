package com.example.studydesk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.studydesk.model.AppDatabase
import com.example.studydesk.repository.UsuarioRepository
import com.example.studydesk.ui.AppNavigation
import com.example.studydesk.ui.theme.StudyDeskTheme
import com.example.studydesk.viewmodel.UsuarioViewModel

class MainActivity : ComponentActivity() {

    // La base de datos se crea sola la primera vez que se abre la app
    private val db by lazy {
        Room.databaseBuilder<AppDatabase>(applicationContext, "studydesk_db")
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    private val usuarioRepository by lazy { UsuarioRepository(db.usuarioDao()) }
    private val usuarioViewModel by lazy { UsuarioViewModel(usuarioRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyDeskTheme {
                AppNavigation(usuarioViewModel)
            }
        }
    }
}