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

    // Pide el permiso de notificaciones; si el usuario lo rechaza, simplemente no se muestran avisos
    private val solicitarPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Notificaciones.crearCanal(this)
        pedirPermisoNotificaciones()

        setContent {
            StudyDeskTheme {
                AppNavigation(usuarioViewModel)
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