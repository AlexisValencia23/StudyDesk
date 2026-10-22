
package com.example.studydesk.ui
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Pestana(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

private val pestanas = listOf(
    Pestana("inicio", "Inicio", Icons.Default.Home),
    Pestana("entregas", "Entregas", Icons.Default.DateRange),
    Pestana("apuntes", "Apuntes", Icons.Default.Edit),
    Pestana("material", "Material", Icons.Default.List)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        bottomBar = {
            if (pestanas.any { it.ruta == rutaActual }) {
                NavigationBar {
                    pestanas.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = {
                                irAPestana(navController, pestana.ruta)
                            },
                            icon = {
                                Icon(
                                    imageVector = pestana.icono,
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
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("login") {
                Login(
                    onLoginSuccess = {
                        navController.navigate("inicio") {
                            popUpTo("login") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onRegistro = {
                        navController.navigate("registro")
                    }
                )
            }

            composable("registro") {
                Registro(
                    onCrearCuenta = { _, _, _, _ ->
                        navController.navigate("inicio") {
                            popUpTo("registro") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onIniciarSesion = {
                        navController.popBackStack()
                    }
                )
            }

            composable("inicio") {
                Inicio(
                    onIrAMaterial = {
                        irAPestana(navController, "material")
                    }
                )
            }

            composable("entregas") {
                Entregas()
            }

            composable("apuntes") {
                Apuntes()
            }

            composable("material") {
                Material()
            }
        }
    }
}

private fun irAPestana(
    navController: NavController,
    ruta: String
) {
    navController.navigate(ruta) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
