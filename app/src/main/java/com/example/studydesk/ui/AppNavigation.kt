package com.example.studydesk.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            Login(
                onLoginSuccess = { nombreIngresado ->
                    navController.navigate("inicio/$nombreIngresado") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegistro = { navController.navigate("registro") }
            )
        }

        composable("registro") {
            Registro(
                onCrearCuenta = { nombre, _, _, _ ->
                    navController.navigate("inicio/$nombre") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIniciarSesion = { navController.popBackStack() }
            )
        }

        composable("inicio/{nombre}") { backStackEntry ->
            val nombreUsuario = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
            Inicio(nombre = nombreUsuario)
        }
    }
}