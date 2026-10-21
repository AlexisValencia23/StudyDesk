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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.ui.theme.StudyDeskTheme

private val Indigo = Color(0xFF3949AB)
private val Fondo = Color(0xFFF6F7FB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)
private val Borde = Color(0xFFE3E6F0)
private val Lavanda = Color(0xFFE8EAF6)

// Datos de ejemplo. Más adelante esto vendrá de la base de datos.
data class MateriaItem(
    val nombre: String,
    val cantidadNormas: Int,
    val cantidadJurisprudencia: Int,
    val ultimaActualizacion: String
)

private val materiasEjemplo = listOf(
    MateriaItem("Derecho Civil", 12, 8, "Hoy"),
    MateriaItem("Derecho Penal", 9, 14, "Ayer"),
    MateriaItem("Derecho Constitucional", 7, 11, "10 oct"),
    MateriaItem("Derecho Laboral", 6, 5, "2 oct")
)

@Composable
fun Material(
    materias: List<MateriaItem> = materiasEjemplo,
    onAbrirMateria: (MateriaItem) -> Unit = {},
    onVerCambios: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)) {
            Text(text = "${materias.size} materias disponibles", fontSize = 12.sp, color = TextoSuave)
            Text(
                text = "Material de estudio",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BannerCambios(onVerCambios)
            }
            items(materias) { materia ->
                TarjetaMateria(materia, onAbrirMateria)
            }
        }
    }
}

@Composable
private fun BannerCambios(onVerCambios: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Lavanda),
        border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cambios normativos recientes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
            Text(
                text = "3 normas fueron modificadas este mes.",
                fontSize = 12.sp,
                color = Color(0xFF283593)
            )
            Button(
                onClick = onVerCambios,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Ver historial de cambios", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TarjetaMateria(
    materia: MateriaItem,
    onAbrirMateria: (MateriaItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = "Actualizado: ${materia.ultimaActualizacion}",
                    fontSize = 12.sp,
                    color = TextoSuave
                )
                Text(
                    text = materia.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EtiquetaConteo("${materia.cantidadNormas} normas")
                EtiquetaConteo("${materia.cantidadJurisprudencia} sentencias")
            }

            OutlinedButton(
                onClick = { onAbrirMateria(materia) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Abrir materia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF283593)
                )
            }
        }
    }
}

@Composable
private fun EtiquetaConteo(texto: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Lavanda) {
        Text(
            text = texto,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF283593),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MaterialPreview() {
    StudyDeskTheme {
        Material()
    }
}