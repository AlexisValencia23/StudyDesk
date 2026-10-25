package com.example.studydesk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.model.Entrega
import com.example.studydesk.ui.theme.StudyDeskTheme

private val Indigo = Color(0xFF3949AB)
private val Fondo = Color(0xFFF6F7FB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)
private val Borde = Color(0xFFE3E6F0)
private val Lavanda = Color(0xFFE8EAF6)

@Composable
fun Entregas(
    entregas: List<Entrega>,
    onGuardarEntrega: (materia: String, titulo: String, fecha: String) -> Unit = { _, _, _ -> },
    onCalendario: () -> Unit = {}
) {
    var mostrarFormulario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)) {
            Text(text = "${entregas.size} pendientes", fontSize = 12.sp, color = TextoSuave)
            Text(
                text = "Entregas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        if (entregas.isEmpty()) {
            Text(
                text = "Aún no tienes entregas. Agrega la primera con el botón de abajo.",
                fontSize = 14.sp,
                color = TextoSuave,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entregas) { entrega ->
                    TarjetaEntrega(entrega)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCalendario,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Indigo),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Calendario", color = Indigo, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { mostrarFormulario = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "+ Agregar entrega", fontWeight = FontWeight.Bold)
            }
        }

        if (mostrarFormulario) {
            FormularioEntrega(
                onGuardar = { materia, titulo, fecha ->
                    onGuardarEntrega(materia, titulo, fecha)
                    mostrarFormulario = false
                },
                onCancelar = { mostrarFormulario = false }
            )
        }
    }
}

@Composable
private fun TarjetaEntrega(entrega: Entrega) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entrega.materia, fontSize = 12.sp, color = TextoSuave)
                Text(
                    text = entrega.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = Lavanda) {
                Text(
                    text = entrega.fecha,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF283593),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// Ventana para crear una entrega nueva; no deja guardar si falta algún campo
@Composable
private fun FormularioEntrega(
    onGuardar: (materia: String, titulo: String, fecha: String) -> Unit,
    onCancelar: () -> Unit
) {
    var materia by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var mostrarErrores by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva entrega") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoEntrega(
                    valor = materia,
                    onCambio = { materia = it },
                    etiqueta = "Materia",
                    mensajeError = if (mostrarErrores && materia.isBlank()) "La materia es obligatoria" else null
                )
                CampoEntrega(
                    valor = titulo,
                    onCambio = { titulo = it },
                    etiqueta = "Nombre del trabajo",
                    mensajeError = if (mostrarErrores && titulo.isBlank()) "El nombre es obligatorio" else null
                )
                CampoEntrega(
                    valor = fecha,
                    onCambio = { fecha = it },
                    etiqueta = "Fecha de entrega (ej: 12 oct)",
                    mensajeError = if (mostrarErrores && fecha.isBlank()) "La fecha es obligatoria" else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (materia.isBlank() || titulo.isBlank() || fecha.isBlank()) {
                        mostrarErrores = true
                    } else {
                        onGuardar(materia, titulo, fecha)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Indigo)
            }
        }
    )
}

@Composable
private fun CampoEntrega(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    mensajeError: String?
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = mensajeError != null,
        supportingText = { if (mensajeError != null) Text(mensajeError) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun EntregasPreview() {
    StudyDeskTheme {
        Entregas(
            entregas = listOf(
                Entrega(1, "Derecho Civil", "Ensayo: responsabilidad contractual", "12 oct"),
                Entrega(2, "Derecho Penal", "Análisis de jurisprudencia", "15 oct")
            )
        )
    }
}