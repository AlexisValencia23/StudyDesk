
package com.example.studydesk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.studydesk.util.SesionManager
import com.example.studydesk.viewmodel.ApunteApiViewModel
import com.example.studydesk.viewmodel.EntregaApiViewModel

@Composable
fun Servidor(
    usuarioId: Int?,
    apunteApiViewModel: ApunteApiViewModel,
    entregaApiViewModel: EntregaApiViewModel,

    // Nuevos parametros para sincronizar apuntes pendientes
    onSincronizarPendientes: (() -> Unit)? = null,
    sincronizandoPendientes: Boolean = false,
    mensajeSincronizacionPendientes: String? = null
) {

    val context = LocalContext.current

    val sesionManager = remember(context) {
        SesionManager(context)
    }

    // Obtener el correo de la cuenta conectada
    val correoUsuario = if (usuarioId != null) {
        sesionManager.obtenerCorreoUsuario()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    } else {
        null
    }

    // Datos obtenidos desde Spring Boot
    val apuntes by
    apunteApiViewModel.apuntes.collectAsState()

    val entregas by
    entregaApiViewModel.entregas.collectAsState()

    // Estados de carga
    val cargandoApuntes by
    apunteApiViewModel.cargando.collectAsState()

    val cargandoEntregas by
    entregaApiViewModel.cargando.collectAsState()

    // Mensajes de error
    val errorApuntes by
    apunteApiViewModel.mensajeError.collectAsState()

    val errorEntregas by
    entregaApiViewModel.mensajeError.collectAsState()

    // Consultar cuando cambie la cuenta
    LaunchedEffect(usuarioId, correoUsuario) {

        apunteApiViewModel.establecerCorreo(
            correoUsuario
        )

        entregaApiViewModel.establecerCorreo(
            correoUsuario
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Datos del servidor",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Información obtenida desde Spring Boot mediante Retrofit.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (usuarioId == null) {

            Text(
                text = "Inicia sesión para consultar tus datos."
            )

        } else if (correoUsuario == null) {

            Text(
                text = "No se encontró el correo de la sesión."
            )

            Text(
                text = "Cierra sesión y vuelve a iniciar sesión para actualizar tus datos."
            )

        } else {

            Text(
                text = "Cuenta: $correoUsuario",
                style = MaterialTheme.typography.bodyMedium
            )

            // APUNTES DEL SERVIDOR

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Apuntes del servidor",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Consultar datos que ya estan en Spring Boot
                    Button(
                        onClick = {
                            apunteApiViewModel.cargarApuntes()
                        },
                        enabled = !cargandoApuntes
                    ) {
                        Text("Actualizar apuntes")
                    }

                    // NUEVO: Enviar apuntes pendientes
                    // Se mostrara cuando conectemos la funcion
                    // desde AppNavigation.kt
                    if (onSincronizarPendientes != null) {

                        Button(
                            onClick = onSincronizarPendientes,
                            enabled = !sincronizandoPendientes
                        ) {
                            Text("Sincronizar pendientes")
                        }

                        Text(
                            text = "Envía los apuntes locales pendientes de vinculación con Spring Boot.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (sincronizandoPendientes) {
                            CircularProgressIndicator()
                        }

                        if (mensajeSincronizacionPendientes != null) {

                            Text(
                                text = mensajeSincronizacionPendientes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (cargandoApuntes) {
                        CircularProgressIndicator()
                    }

                    if (errorApuntes != null) {

                        Text(
                            text = errorApuntes ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (
                        !cargandoApuntes &&
                        errorApuntes == null &&
                        apuntes.isEmpty()
                    ) {
                        Text(
                            text = "No hay apuntes asociados a este correo en el servidor."
                        )
                    }

                    apuntes.forEach { apunte ->

                        HorizontalDivider()

                        Text(
                            text = apunte.titulo,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Materia: ${apunte.materia}"
                        )

                        Text(
                            text = "Fecha: ${apunte.fecha}"
                        )

                        Text(
                            text = "Contenido: ${apunte.contenido}"
                        )
                    }
                }
            }

            // ENTREGAS DEL SERVIDOR

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Entregas del servidor",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = {
                            entregaApiViewModel.cargarEntregas()
                        },
                        enabled = !cargandoEntregas
                    ) {
                        Text("Actualizar entregas")
                    }

                    if (cargandoEntregas) {
                        CircularProgressIndicator()
                    }

                    if (errorEntregas != null) {

                        Text(
                            text = errorEntregas ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (
                        !cargandoEntregas &&
                        errorEntregas == null &&
                        entregas.isEmpty()
                    ) {
                        Text(
                            text = "No hay entregas asociadas a este correo en el servidor."
                        )
                    }

                    entregas.forEach { entrega ->

                        HorizontalDivider()

                        Text(
                            text = entrega.titulo,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "Materia: ${entrega.materia}"
                        )

                        Text(
                            text = "Fecha: ${entrega.fecha}"
                        )
                    }
                }
            }
        }
    }
}
