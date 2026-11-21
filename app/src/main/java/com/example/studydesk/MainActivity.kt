
package com.example.studydesk

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.studydesk.util.IdentificadorSync
import com.example.studydesk.util.SesionManager
import com.example.studydesk.viewmodel.ApunteViewModel
import com.example.studydesk.viewmodel.ComentarioViewModel
import com.example.studydesk.viewmodel.EntregaViewModel
import com.example.studydesk.viewmodel.UsuarioViewModel


class MainActivity : ComponentActivity() {

    // Base de datos local Room
    private val db by lazy {
        Room.databaseBuilder<AppDatabase>(
            applicationContext,
            "studydesk_db"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    // Identificador persistente de sincronizacion
    private val identificadorSync by lazy {
        IdentificadorSync(applicationContext)
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
        EntregaViewModel(
            repository = entregaRepository,
            identificadorSync = identificadorSync
        )
    }

    // Apuntes
    private val apunteRepository by lazy {
        ApunteRepository(db.apunteDao())
    }

    private val apunteViewModel by lazy {
        ApunteViewModel(
            repository = apunteRepository,
            identificadorSync = identificadorSync
        )
    }

    // Comentarios
    private val comentarioRepository by lazy {
        ComentarioRepository(db.comentarioDao())
    }

    private val comentarioViewModel by lazy {
        ComentarioViewModel(comentarioRepository)
    }

    // Permiso de notificaciones
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

    // Sincronizacion automatica al abrir o regresar a StudyDesk
    override fun onResume() {
        super.onResume()

        val sesion = SesionManager(applicationContext)

        val usuarioId = sesion.obtenerIdUsuario()

        val correo = sesion.obtenerCorreoUsuario()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (usuarioId == null || correo == null) {
            return
        }

        apunteViewModel.establecerUsuario(usuarioId)
        apunteViewModel.establecerCorreo(correo)

        entregaViewModel.establecerUsuario(usuarioId)
        entregaViewModel.establecerCorreo(correo)

        Log.d(
            "StudyDeskSync",
            "Comprobando sincronizacion automatica al abrir StudyDesk"
        )

        apunteViewModel.sincronizarPendientes()
        entregaViewModel.sincronizarPendientes()
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
