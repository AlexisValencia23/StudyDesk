
package com.example.studydesk

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.studydesk.model.AppDatabase
import com.example.studydesk.notification.Notificaciones
import com.example.studydesk.repository.ApunteRepository
import com.example.studydesk.repository.ComentarioRepository
import com.example.studydesk.repository.EntregaRepository
import com.example.studydesk.repository.UsuarioRepository
import com.example.studydesk.ui.AppNavigation
import com.example.studydesk.ui.theme.StudyDeskTheme
import com.example.studydesk.viewmodel.ApunteViewModel
import com.example.studydesk.viewmodel.ComentarioViewModel
import com.example.studydesk.viewmodel.EntregaViewModel
import com.example.studydesk.viewmodel.UsuarioViewModel

class MainActivity : ComponentActivity() {

    // Crear la base de datos Room
    private val db by lazy {
        Room.databaseBuilder<AppDatabase>(
            applicationContext,
            "studydesk_db"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    // Usuarios
    private val usuarioRepository by lazy {
        UsuarioRepository(db.usuarioDao())
    }

    private val usuarioViewModel by lazy {
        UsuarioViewModel(usuarioRepository)
    }

    // Entregas
    private val entregaRepository by lazy {
        EntregaRepository(db.entregaDao())
    }

    private val entregaViewModel by lazy {
        EntregaViewModel(entregaRepository)
    }

    // Apuntes
    private val apunteRepository by lazy {
        ApunteRepository(db.apunteDao())
    }

    private val apunteViewModel by lazy {
        ApunteViewModel(apunteRepository)
    }

    // Comentarios
    private val comentarioRepository by lazy {
        ComentarioRepository(db.comentarioDao())
    }

    private val comentarioViewModel by lazy {
        ComentarioViewModel(comentarioRepository)
    }

    // Solicitar permiso para las notificaciones
    private val solicitarPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Notificaciones.crearCanal(this)
        pedirPermisoNotificaciones()

        setContent {
            StudyDeskTheme {
                AppNavigation(
                    usuarioViewModel = usuarioViewModel,
                    entregaViewModel = entregaViewModel,
                    apunteViewModel = apunteViewModel,
                    comentarioViewModel = comentarioViewModel
                )
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notificaciones.tienePermiso(this)
        ) {
            solicitarPermisoNotificaciones.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}
