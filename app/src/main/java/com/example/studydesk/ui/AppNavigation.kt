package com.example.studydesk.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studydesk.model.Apunte
import com.example.studydesk.notification.Notificaciones
import com.example.studydesk.viewmodel.ApunteViewModel
import com.example.studydesk.viewmodel.EntregaViewModel
import com.example.studydesk.viewmodel.UsuarioViewModel

// Pestañas de la barra inferior (solo las 4 pantallas principales)
private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana("inicio", "Inicio", Icons.Default.Home),
    Pestana("entregas", "Entregas", Icons.Default.DateRange),
    Pestana("apuntes", "Apuntes", Icons.Default.Edit),
    Pestana("material", "Material", Icons.Default.Folder)
)

@Composable
fun AppNavigation(
    usuarioViewModel: UsuarioViewModel,
    entregaViewModel: EntregaViewModel,
    apunteViewModel: ApunteViewModel
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val context = LocalContext.current
    val entregas by entregaViewModel.entregas.collectAsState()
    val apuntes by apunteViewModel.apuntes.collectAsState()

    // Nombre del usuario que inició sesión
    var nombreUsuario by remember { mutableStateOf("Usuario") }

    // Muestra el modal "Cuenta creada" después de registrarse
    var mostrarCuentaCreada by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // La barra solo se muestra en las 4 pantallas principales
            if (pestanas.any { it.ruta == rutaActual }) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = { irAPestana(navController, pestana.ruta) },
                            icon = { Icon(pestana.icono, contentDescription = pestana.titulo) },
                            label = { Text(pestana.titulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                Login(
                    mensajeError = usuarioViewModel.mensajeError,
                    onIniciarSesion = { correo, contrasena ->
                        usuarioViewModel.iniciarSesion(correo, contrasena) { nombreGuardado ->
                            nombreUsuario = nombreGuardado
                            navController.navigate("inicio") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onRegistro = {
                        usuarioViewModel.limpiarError()
                        navController.navigate("registro")
                    }
                )
            }

            composable("registro") {
                Registro(
                    mensajeError = usuarioViewModel.mensajeError,
                    mostrarCuentaCreada = mostrarCuentaCreada,
                    onAceptarCuentaCreada = {
                        mostrarCuentaCreada = false
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onCrearCuenta = { nombre, correo, contrasena, perfil ->
                        usuarioViewModel.registrar(nombre, correo, contrasena, perfil) { nombreGuardado ->
                            nombreUsuario = nombreGuardado
                            mostrarCuentaCreada = true
                        }
                    },
                    onIniciarSesion = {
                        usuarioViewModel.limpiarError()
                        navController.popBackStack()
                    }
                )
            }

            composable("inicio") {
                Inicio(
                    nombre = nombreUsuario,
                    onIrAMaterial = { irAPestana(navController, "material") },
                    onSimularAviso = {
                        Notificaciones.mostrarAvisoCambio(
                            context,
                            "Cambio normativo",
                            "Se modificó el Código del Trabajo (aviso de ejemplo)"
                        )
                    }
                )
            }

            composable("entregas") {
                Entregas(
                    entregas = entregas,
                    onGuardarEntrega = { materia, titulo, fecha ->
                        entregaViewModel.agregarEntrega(materia, titulo, fecha)
                    }
                )
            }
            composable("apuntes") {
                Apuntes(
                    apuntes = apuntes,
                    onGuardarApunte = { materia, titulo, contenido ->
                        apunteViewModel.agregarApunte(materia, titulo, contenido)
                    },
                    onEditarApunte = { apunte, titulo, contenido ->
                        apunteViewModel.editarApunte(apunte, titulo, contenido)
                    },
                    onCompartir = { apunte -> compartirApunte(context, apunte) }
                )
            }
            composable("material") { Material() }
        }
    }
}

// Cambia de pestaña sin apilar pantallas repetidas
private fun irAPestana(navController: NavController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo("inicio") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Abre el selector de Android para enviar el apunte como texto (WhatsApp, correo, etc.)
private fun compartirApunte(context: Context, apunte: Apunte) {
    val texto = "${apunte.titulo} (${apunte.materia})\n\n${apunte.contenido}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, apunte.titulo)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir apunte"))
}