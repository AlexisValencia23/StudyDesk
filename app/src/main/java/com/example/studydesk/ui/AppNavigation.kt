
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.studydesk.model.materialesEstudio
import com.example.studydesk.notification.Notificaciones
import com.example.studydesk.util.SesionManager
import com.example.studydesk.viewmodel.ApunteViewModel
import com.example.studydesk.viewmodel.ComentarioViewModel
import com.example.studydesk.viewmodel.EntregaViewModel
import com.example.studydesk.viewmodel.UsuarioViewModel

private data class Pestana(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

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
    apunteViewModel: ApunteViewModel,
    comentarioViewModel: ComentarioViewModel
) {
    val navController = rememberNavController()

    val entradaActual by
    navController.currentBackStackEntryAsState()

    val rutaActual = entradaActual?.destination?.route

    val context = LocalContext.current
    val sesion = remember { SesionManager(context) }

    // Listas de Room
    val entregas by entregaViewModel.entregas.collectAsState()
    val apuntes by apunteViewModel.apuntes.collectAsState()

    // Usuario conectado
    var usuarioIdActivo by remember {
        mutableStateOf(sesion.obtenerIdUsuario())
    }

    // Actualizar todos los ViewModels al cambiar de usuario
    LaunchedEffect(usuarioIdActivo) {
        apunteViewModel.establecerUsuario(usuarioIdActivo)
        entregaViewModel.establecerUsuario(usuarioIdActivo)
        comentarioViewModel.establecerUsuario(usuarioIdActivo)
    }

    // Mostrar solamente datos propios
    val apuntesDelUsuario = apuntes.filter {
        it.usuarioId == usuarioIdActivo
    }

    val entregasDelUsuario = entregas.filter {
        it.usuarioId == usuarioIdActivo
    }

    val rutaInicial = remember {
        if (sesion.obtenerIdUsuario() != null) {
            "inicio"
        } else {
            "login"
        }
    }

    var nombreUsuario by remember {
        mutableStateOf(
            sesion.obtenerNombre() ?: "Usuario"
        )
    }

    var mostrarCuentaCreada by remember {
        mutableStateOf(false)
    }

    Scaffold(
        bottomBar = {
            if (pestanas.any { it.ruta == rutaActual }) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = {
                                irAPestana(
                                    navController,
                                    pestana.ruta
                                )
                            },
                            icon = {
                                Icon(
                                    pestana.icono,
                                    contentDescription = pestana.titulo
                                )
                            },
                            label = {
                                Text(pestana.titulo)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = rutaInicial,
            modifier = Modifier.padding(innerPadding)
        ) {

            // LOGIN
            composable("login") {
                Login(
                    mensajeError = usuarioViewModel.mensajeError,

                    onIniciarSesion = { correo, contrasena ->
                        usuarioViewModel.iniciarSesion(
                            correo,
                            contrasena
                        ) { usuario ->

                            nombreUsuario = usuario.nombre
                            sesion.guardarSesion(usuario)

                            usuarioIdActivo = usuario.id

                            apunteViewModel.establecerUsuario(
                                usuario.id
                            )

                            entregaViewModel.establecerUsuario(
                                usuario.id
                            )

                            comentarioViewModel.establecerUsuario(
                                usuario.id
                            )

                            navController.navigate("inicio") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }
                    },

                    onRegistro = {
                        usuarioViewModel.limpiarError()
                        navController.navigate("registro")
                    }
                )
            }

            // REGISTRO
            composable("registro") {
                Registro(
                    mensajeError = usuarioViewModel.mensajeError,
                    mostrarCuentaCreada = mostrarCuentaCreada,

                    onAceptarCuentaCreada = {
                        mostrarCuentaCreada = false

                        navController.navigate("inicio") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    },

                    onCrearCuenta = {
                            nombre,
                            correo,
                            contrasena,
                            perfil ->

                        usuarioViewModel.registrar(
                            nombre,
                            correo,
                            contrasena,
                            perfil
                        ) { usuario ->

                            nombreUsuario = usuario.nombre
                            sesion.guardarSesion(usuario)

                            usuarioIdActivo = usuario.id

                            apunteViewModel.establecerUsuario(
                                usuario.id
                            )

                            entregaViewModel.establecerUsuario(
                                usuario.id
                            )

                            comentarioViewModel.establecerUsuario(
                                usuario.id
                            )

                            mostrarCuentaCreada = true
                        }
                    },

                    onIniciarSesion = {
                        usuarioViewModel.limpiarError()
                        navController.popBackStack()
                    },

                    onCambiarCorreo = {
                        usuarioViewModel.limpiarError()
                    }
                )
            }

            // INICIO
            composable("inicio") {
                Inicio(
                    nombre = nombreUsuario,
                    entregas = entregasDelUsuario,
                    apuntes = apuntesDelUsuario,

                    onIrAMaterial = {
                        irAPestana(navController, "material")
                    },

                    onSimularAviso = {
                        Notificaciones.mostrarAvisoCambio(
                            context,
                            "Cambio normativo",
                            "Se modificó el Código del Trabajo (aviso de ejemplo)"
                        )
                    },

                    onCerrarSesion = {
                        sesion.cerrarSesion()
                        usuarioViewModel.limpiarError()

                        usuarioIdActivo = null

                        apunteViewModel.establecerUsuario(null)
                        entregaViewModel.establecerUsuario(null)
                        comentarioViewModel.establecerUsuario(null)

                        nombreUsuario = "Usuario"

                        navController.navigate("login") {
                            popUpTo("inicio") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // ENTREGAS
            composable("entregas") {
                Entregas(
                    entregas = entregasDelUsuario,

                    onGuardarEntrega = { materia, titulo, fecha ->
                        entregaViewModel.agregarEntrega(
                            materia,
                            titulo,
                            fecha
                        )
                    },

                    onEliminarEntrega = { entrega ->
                        entregaViewModel.eliminarEntrega(entrega)
                    }
                )
            }

            // APUNTES
            composable("apuntes") {
                Apuntes(
                    apuntes = apuntesDelUsuario,

                    onGuardarApunte = {
                            materia, titulo, contenido ->

                        apunteViewModel.agregarApunte(
                            materia,
                            titulo,
                            contenido
                        )
                    },

                    onEditarApunte = {
                            apunte, titulo, contenido ->

                        apunteViewModel.editarApunte(
                            apunte,
                            titulo,
                            contenido
                        )
                    },

                    onCompartir = { apunte ->
                        compartirApunte(context, apunte)
                    },

                    onEliminarApunte = { apunte ->
                        apunteViewModel.eliminarApunte(apunte)
                    }
                )
            }

            // MATERIAL DE ESTUDIO
            composable("material") {
                Material(
                    onAbrirMaterial = { material ->
                        navController.navigate(
                            "visor/${material.archivoPdf}"
                        )
                    },

                    obtenerComentarios =
                        comentarioViewModel::comentarios,

                    // Identificar al autor por su ID
                    usuarioId = usuarioIdActivo,

                    onAgregarComentario = {
                            archivoPdf, texto ->

                        comentarioViewModel.agregarComentario(
                            archivoPdf,
                            nombreUsuario,
                            texto
                        )
                    },

                    onEditarComentario = {
                            comentario, texto ->

                        comentarioViewModel.editarComentario(
                            comentario,
                            texto
                        )
                    },

                    onEliminarComentario = {
                            comentario ->
                        comentarioViewModel.eliminarComentario(
                            comentario
                        )
                    }
                )
            }

            // VISOR PDF
            composable("visor/{archivo}") { entrada ->
                val archivo = entrada.arguments
                    ?.getString("archivo") ?: ""

                val titulo = materialesEstudio.firstOrNull {
                    it.archivoPdf == archivo
                }?.titulo ?: "Material"

                VisorPdf(
                    titulo = titulo,
                    nombreArchivo = archivo,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

private fun irAPestana(
    navController: NavController,
    ruta: String
) {
    navController.navigate(ruta) {
        popUpTo("inicio") {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

// Compartir apuntes con el selector de Android
private fun compartirApunte(
    context: Context,
    apunte: Apunte
) {
    val texto =
        "${apunte.titulo} (${apunte.materia})\n\n${apunte.contenido}"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"

        putExtra(
            Intent.EXTRA_SUBJECT,
            apunte.titulo
        )

        putExtra(
            Intent.EXTRA_TEXT,
            texto
        )
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Compartir apunte"
        )
    )
}
