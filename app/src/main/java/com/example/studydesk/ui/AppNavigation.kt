package com.example.studydesk.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Pestañas de la barra inferior (solo las 4 pantallas principales)
private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

private val pestanas = listOf(
    Pestana("inicio", "Inicio", Icons.Default.Home),
    Pestana("entregas", "Entregas", Icons.Default.DateRange),
    Pestana("apuntes", "Apuntes", Icons.Default.Edit),
    Pestana("material", "Material", Icons.Default.Folder)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Nombre del usuario que inició sesión (más adelante saldrá de la base de datos)
    var nombreUsuario by remember { mutableStateOf("Usuario") }

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
                    onLoginSuccess = { nombreIngresado ->
                        nombreUsuario = nombreIngresado
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegistro = { navController.navigate("registro") }
                )
            }

            composable("registro") {
                Registro(
                    onCrearCuenta = { nombre, _, _, _ ->
                        nombreUsuario = nombre
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onIniciarSesion = { navController.popBackStack() }
                )
            }

            composable("inicio") {
                Inicio(
                    nombre = nombreUsuario,
                    onIrAMaterial = { irAPestana(navController, "material") }
                )
            }

            composable("entregas") { Entregas() }
            composable("apuntes") { Apuntes() }
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