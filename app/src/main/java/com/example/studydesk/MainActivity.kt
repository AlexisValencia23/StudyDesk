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

    // La base de datos se crea sola la primera vez que se abre la app
    private val db by lazy {
        Room.databaseBuilder<AppDatabase>(applicationContext, "studydesk_db")
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    private val usuarioRepository by lazy { UsuarioRepository(db.usuarioDao()) }
    private val usuarioViewModel by lazy { UsuarioViewModel(usuarioRepository) }

    private val entregaRepository by lazy { EntregaRepository(db.entregaDao()) }
    private val entregaViewModel by lazy { EntregaViewModel(entregaRepository) }

    private val apunteRepository by lazy { ApunteRepository(db.apunteDao()) }
    private val apunteViewModel by lazy { ApunteViewModel(apunteRepository) }

    private val comentarioRepository by lazy { ComentarioRepository(db.comentarioDao()) }
    private val comentarioViewModel by lazy { ComentarioViewModel(comentarioRepository) }

    // Pide el permiso de notificaciones; si el usuario lo rechaza, simplemente no se muestran avisos
    private val solicitarPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Notificaciones.crearCanal(this)
        pedirPermisoNotificaciones()

        setContent {
            StudyDeskTheme {
                AppNavigation(usuarioViewModel, entregaViewModel, apunteViewModel, comentarioViewModel)
            }
        }
    }

    // Desde Android 13 el permiso hay que pedirlo en pantalla
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificaciones.tienePermiso(this)) {
            solicitarPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}