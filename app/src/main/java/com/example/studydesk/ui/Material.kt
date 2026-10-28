package com.example.studydesk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.studydesk.model.Comentario
import com.example.studydesk.model.MaterialEstudio
import com.example.studydesk.model.materialesEstudio
import com.example.studydesk.ui.theme.StudyDeskTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private val Indigo = Color(0xFF3949AB)
private val Fondo = Color(0xFFF6F7FB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)
private val Borde = Color(0xFFE3E6F0)

@Composable
fun Material(
    materiales: List<MaterialEstudio> = materialesEstudio,
    onAbrirMaterial: (MaterialEstudio) -> Unit = {},
    obtenerComentarios: (archivoPdf: String) -> Flow<List<Comentario>> = { emptyFlow() },
    onAgregarComentario: (archivoPdf: String, texto: String) -> Unit = { _, _ -> }
) {
    var busqueda by remember { mutableStateOf("") }
    var materialComentado by remember { mutableStateOf<MaterialEstudio?>(null) }

    // Busca por materia, título o descripción
    val textoBuscado = busqueda.trim()
    val visibles = materiales.filter {
        it.materia.contains(textoBuscado, ignoreCase = true) ||
                it.titulo.contains(textoBuscado, ignoreCase = true) ||
                it.descripcion.contains(textoBuscado, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)) {
            Text(text = "Legislación y material por materia", fontSize = 12.sp, color = TextoSuave)
            Text(
                text = "Material de estudio",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar materia o material") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = Indigo) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (visibles.isEmpty()) {
                item {
                    Text(
                        text = "No se encontró material para \"$busqueda\".",
                        fontSize = 14.sp,
                        color = TextoSuave,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            items(visibles) { material ->
                TarjetaMaterial(material, onAbrirMaterial, onComentarios = { materialComentado = it })
            }
        }

        materialComentado?.let { material ->
            DialogoComentarios(
                material = material,
                obtenerComentarios = obtenerComentarios,
                onPublicar = { texto -> onAgregarComentario(material.archivoPdf, texto) },
                onCerrar = { materialComentado = null }
            )
        }
    }
}

@Composable
private fun TarjetaMaterial(
    material: MaterialEstudio,
    onAbrirMaterial: (MaterialEstudio) -> Unit,
    onComentarios: (MaterialEstudio) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = material.materia, fontSize = 12.sp, color = TextoSuave)
            Text(
                text = material.titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(text = material.descripcion, fontSize = 13.sp, color = TextoSuave)
            Text(
                text = "Última actualización: ${material.fechaActualizacion}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Indigo
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAbrirMaterial(material) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Abrir material", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onComentarios(material) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Indigo),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Comentarios", color = Indigo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Ventana con los comentarios de un material y un campo para escribir uno nuevo
@Composable
private fun DialogoComentarios(
    material: MaterialEstudio,
    obtenerComentarios: (archivoPdf: String) -> Flow<List<Comentario>>,
    onPublicar: (texto: String) -> Unit,
    onCerrar: () -> Unit
) {
    val comentarios by remember(material.archivoPdf) { obtenerComentarios(material.archivoPdf) }
        .collectAsState(initial = emptyList())

    var nuevoTexto by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Comentarios: ${material.titulo}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (comentarios.isEmpty()) {
                    Text(text = "Aún no hay comentarios.", fontSize = 14.sp, color = TextoSuave)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comentarios) { comentario ->
                            FilaComentario(comentario)
                        }
                    }
                }

                OutlinedTextField(
                    value = nuevoTexto,
                    onValueChange = { nuevoTexto = it },
                    label = { Text("Escribe un comentario") },
                    isError = mostrarError && nuevoTexto.isBlank(),
                    supportingText = {
                        if (mostrarError && nuevoTexto.isBlank()) Text("Escribe algo antes de publicar")
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nuevoTexto.isBlank()) {
                        mostrarError = true
                    } else {
                        onPublicar(nuevoTexto)
                        nuevoTexto = ""
                        mostrarError = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo)
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar", color = Indigo)
            }
        }
    )
}

@Composable
private fun FilaComentario(comentario: Comentario) {
    Column {
        Text(
            text = "${comentario.autor} · ${comentario.fecha}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextoSuave
        )
        Text(text = comentario.texto, fontSize = 14.sp, color = TextoOscuro)
    }
}

@Preview(showBackground = true)
@Composable
fun MaterialPreview() {
    StudyDeskTheme {
        Material()
    }
}