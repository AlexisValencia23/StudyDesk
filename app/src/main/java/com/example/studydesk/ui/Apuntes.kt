
package com.example.studydesk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.model.Apunte
import com.example.studydesk.ui.theme.StudyDeskTheme
import com.example.studydesk.util.Validaciones

// Colores de StudyDesk
private val Indigo = Color(0xFF3949AB)
private val Fondo = Color(0xFFF6F7FB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)
private val Borde = Color(0xFFE3E6F0)
private val RojoEliminar = Color(0xFFB3261E)

private const val TODAS = "Todas"

// Pantalla principal de Apuntes
@Composable
fun Apuntes(
    apuntes: List<Apunte>,
    onGuardarApunte: (
        materia: String,
        titulo: String,
        contenido: String
    ) -> Unit = { _, _, _ -> },
    onEditarApunte: (
        apunte: Apunte,
        titulo: String,
        contenido: String
    ) -> Unit = { _, _, _ -> },
    onCompartir: (Apunte) -> Unit = {},
    onEliminarApunte: (Apunte) -> Unit = {}
) {
    var filtro by remember {
        mutableStateOf(TODAS)
    }

    var mostrarFormulario by remember {
        mutableStateOf(false)
    }

    var apunteParaVer by remember {
        mutableStateOf<Apunte?>(null)
    }

    var apunteParaEditar by remember {
        mutableStateOf<Apunte?>(null)
    }

    // Nuevo estado: apunte seleccionado para eliminar
    var apunteParaEliminar by remember {
        mutableStateOf<Apunte?>(null)
    }

    // Obtener materias disponibles
    val materias = listOf(TODAS) +
            apuntes.map { it.materia }.distinct()

    // Filtrar apuntes según la materia
    val visibles = if (filtro == TODAS) {
        apuntes
    } else {
        apuntes.filter { it.materia == filtro }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {

        // Encabezado
        Column(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 28.dp,
                bottom = 12.dp
            )
        ) {
            Text(
                text = if (apuntes.size == 1)
                    "1 apunte guardado"
                else
                    "${apuntes.size} apuntes guardados",
                fontSize = 12.sp,
                color = TextoSuave
            )

            Text(
                text = "Apuntes",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        // Mostrar mensaje si no existen apuntes
        if (apuntes.isEmpty()) {
            Text(
                text = "Aún no tienes apuntes. Crea el primero con el botón de abajo.",
                fontSize = 14.sp,
                color = TextoSuave,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            )
        } else {

            // Filtros por materia
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = 20.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(materias) { materia ->
                    FilterChip(
                        selected = filtro == materia,
                        onClick = {
                            filtro = materia
                        },
                        label = {
                            Text(materia)
                        }
                    )
                }
            }

            // Lista de apuntes guardados
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = visibles,
                    key = { it.id }
                ) { apunte ->
                    TarjetaApunte(
                        apunte = apunte,
                        onVer = {
                            apunteParaVer = it
                        },
                        onEditar = {
                            apunteParaEditar = it
                        },
                        onCompartir = onCompartir,
                        onEliminar = {
                            apunteParaEliminar = it
                        }
                    )
                }
            }
        }

        // Botón para agregar un apunte
        Button(
            onClick = {
                mostrarFormulario = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Indigo
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 14.dp
                )
                .height(48.dp)
        ) {
            Text(
                text = "+ Nuevo apunte",
                fontWeight = FontWeight.Bold
            )
        }

        // Formulario para crear un apunte
        if (mostrarFormulario) {
            FormularioApunte(
                apunteInicial = null,
                onGuardar = { materia, titulo, contenido ->
                    onGuardarApunte(
                        materia,
                        titulo,
                        contenido
                    )
                    mostrarFormulario = false
                },
                onCancelar = {
                    mostrarFormulario = false
                }
            )
        }

        // Formulario para editar un apunte
        apunteParaEditar?.let { apunte ->
            FormularioApunte(
                apunteInicial = apunte,
                onGuardar = { _, titulo, contenido ->
                    onEditarApunte(
                        apunte,
                        titulo,
                        contenido
                    )
                    apunteParaEditar = null
                },
                onCancelar = {
                    apunteParaEditar = null
                }
            )
        }

        // Ventana para ver el contenido completo
        apunteParaVer?.let { apunte ->
            DialogoVerApunte(
                apunte = apunte,
                onCerrar = {
                    apunteParaVer = null
                }
            )
        }

        // Nueva ventana: confirmar eliminación
        apunteParaEliminar?.let { apunte ->
            AlertDialog(
                onDismissRequest = {
                    apunteParaEliminar = null
                },
                title = {
                    Text("Eliminar apunte")
                },
                text = {
                    Text(
                        "¿Estás seguro de que deseas eliminar " +
                                "\"${apunte.titulo}\"? " +
                                "Esta acción no se puede deshacer."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEliminarApunte(apunte)
                            apunteParaEliminar = null
                        }
                    ) {
                        Text(
                            text = "Eliminar",
                            color = RojoEliminar,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            apunteParaEliminar = null
                        }
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Indigo
                        )
                    }
                }
            )
        }
    }
}

// Tarjeta individual de cada apunte
@Composable
private fun TarjetaApunte(
    apunte: Apunte,
    onVer: (Apunte) -> Unit,
    onEditar: (Apunte) -> Unit,
    onCompartir: (Apunte) -> Unit,
    onEliminar: (Apunte) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Información del apunte
            Column {
                Text(
                    text = "${apunte.materia} · ${apunte.fecha}",
                    fontSize = 12.sp,
                    color = TextoSuave
                )

                Text(
                    text = apunte.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }

            // Botones existentes
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonAccion(
                    texto = "Ver",
                    modifier = Modifier.weight(1f)
                ) {
                    onVer(apunte)
                }

                BotonAccion(
                    texto = "Editar",
                    modifier = Modifier.weight(1f)
                ) {
                    onEditar(apunte)
                }

                BotonAccion(
                    texto = "Compartir",
                    modifier = Modifier.weight(1f)
                ) {
                    onCompartir(apunte)
                }
            }

            // Nuevo botón para eliminar
            TextButton(
                onClick = {
                    onEliminar(apunte)
                }
            ) {
                Text(
                    text = "Eliminar",
                    color = RojoEliminar,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Botón reutilizable para las acciones del apunte
@Composable
private fun BotonAccion(
    texto: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            Color(0xFFC5CAE9)
        ),
        contentPadding = PaddingValues(
            horizontal = 4.dp
        ),
        modifier = modifier.height(44.dp)
    ) {
        Text(
            text = texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF283593)
        )
    }
}

// Formulario para crear y editar apuntes
@Composable
private fun FormularioApunte(
    apunteInicial: Apunte?,
    onGuardar: (
        materia: String,
        titulo: String,
        contenido: String
    ) -> Unit,
    onCancelar: () -> Unit
) {
    val editando = apunteInicial != null

    var materia by remember(apunteInicial?.id) {
        mutableStateOf(apunteInicial?.materia ?: "")
    }

    var titulo by remember(apunteInicial?.id) {
        mutableStateOf(apunteInicial?.titulo ?: "")
    }

    var contenido by remember(apunteInicial?.id) {
        mutableStateOf(apunteInicial?.contenido ?: "")
    }

    var mostrarErrores by remember(apunteInicial?.id) {
        mutableStateOf(false)
    }

    // Validaciones centralizadas
    val materiaError = if (mostrarErrores && !editando) {
        Validaciones.validarMateriaApunte(materia)
    } else null

    val tituloError = if (mostrarErrores) {
        Validaciones.validarTituloApunte(titulo)
    } else null

    val contenidoError = if (mostrarErrores) {
        Validaciones.validarContenidoApunte(contenido)
    } else null

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                if (editando)
                    "Editar apunte"
                else
                    "Nuevo apunte"
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // La materia solo se ingresa al crear
                if (!editando) {
                    CampoApunte(
                        valor = materia,
                        onCambio = {
                            materia = it
                        },
                        etiqueta = "Materia",
                        mensajeError = materiaError
                    )
                }

                // Título del apunte
                CampoApunte(
                    valor = titulo,
                    onCambio = {
                        titulo = it
                    },
                    etiqueta = "Título",
                    mensajeError = tituloError
                )

                // Contenido del apunte
                CampoApunte(
                    valor = contenido,
                    onCambio = {
                        contenido = it
                    },
                    etiqueta = "Contenido",
                    mensajeError = contenidoError,
                    unaLinea = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    mostrarErrores = true

                    val materiaValida = editando ||
                            Validaciones.validarMateriaApunte(materia) == null

                    val tituloValido =
                        Validaciones.validarTituloApunte(titulo) == null

                    val contenidoValido =
                        Validaciones.validarContenidoApunte(contenido) == null

                    // Guardar únicamente datos válidos
                    if (
                        materiaValida &&
                        tituloValido &&
                        contenidoValido
                    ) {
                        onGuardar(
                            materia.trim(),
                            titulo.trim(),
                            contenido.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo
                )
            ) {
                Text(
                    if (editando)
                        "Guardar cambios"
                    else
                        "Guardar"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar
            ) {
                Text(
                    text = "Cancelar",
                    color = Indigo
                )
            }
        }
    )
}

// Ventana para leer un apunte completo
@Composable
private fun DialogoVerApunte(
    apunte: Apunte,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(apunte.titulo)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${apunte.materia} · ${apunte.fecha}",
                    fontSize = 12.sp,
                    color = TextoSuave
                )

                Text(
                    text = apunte.contenido,
                    fontSize = 15.sp,
                    color = TextoOscuro
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onCerrar
            ) {
                Text(
                    text = "Cerrar",
                    color = Indigo
                )
            }
        }
    )
}

// Campo reutilizable con mensajes de validación
@Composable
private fun CampoApunte(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    mensajeError: String?,
    unaLinea: Boolean = true
) {
    val icono = when (etiqueta) {
        "Materia" -> Icons.Default.List
        "Título" -> Icons.Default.Edit
        else -> Icons.Default.Info
    }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = {
            Text(etiqueta)
        },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = null
            )
        },
        isError = mensajeError != null,
        supportingText = {
            if (mensajeError != null) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        modifier = Modifier.fillMaxWidth()
    )
}

// Vista previa
@Preview(showBackground = true)
@Composable
fun ApuntesPreview() {
    StudyDeskTheme {
        Apuntes(
            apuntes = listOf(
                Apunte(
                    1,
                    "Derecho Constitucional",
                    "Garantías constitucionales",
                    "Resumen del tema",
                    "Hoy"
                ),
                Apunte(
                    2,
                    "Derecho Laboral",
                    "Despido: causales",
                    "Resumen del tema",
                    "Ayer"
                )
            )
        )
    }
}
