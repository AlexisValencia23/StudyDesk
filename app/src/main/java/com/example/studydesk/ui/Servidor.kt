
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studydesk.viewmodel.ApunteApiViewModel
import com.example.studydesk.viewmodel.EntregaApiViewModel

@Composable
fun Servidor(
    usuarioId: Int?,
    apunteApiViewModel: ApunteApiViewModel,
    entregaApiViewModel: EntregaApiViewModel
) {

    // Datos de Spring Boot
    val apuntes by apunteApiViewModel.apuntes.collectAsState()
    val entregas by entregaApiViewModel.entregas.collectAsState()

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

    // Consultar datos cuando cambie el usuario
    LaunchedEffect(usuarioId) {
        apunteApiViewModel.establecerUsuario(usuarioId)
        entregaApiViewModel.establecerUsuario(usuarioId)
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

            Text("Inicia sesión para consultar tus datos.")

        } else {

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

                    Button(
                        onClick = {
                            apunteApiViewModel.cargarApuntes()
                        }
                    ) {
                        Text("Actualizar apuntes")
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

                    if (!cargandoApuntes && apuntes.isEmpty()) {
                        Text("No hay apuntes en el servidor.")
                    }

                    apuntes.forEach { apunte ->

                        HorizontalDivider()

                        Text(
                            text = apunte.titulo,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text("Materia: ${apunte.materia}")
                        Text("Fecha: ${apunte.fecha}")
                        Text("Contenido: ${apunte.contenido}")
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
                        }
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

                    if (!cargandoEntregas && entregas.isEmpty()) {
                        Text("No hay entregas en el servidor.")
                    }

                    entregas.forEach { entrega ->

                        HorizontalDivider()

                        Text(
                            text = entrega.titulo,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text("Materia: ${entrega.materia}")
                        Text("Fecha: ${entrega.fecha}")
                    }
                }
            }
        }
    }
}
