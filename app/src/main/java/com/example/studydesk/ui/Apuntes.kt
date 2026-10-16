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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.studydesk.ui.theme.StudyDeskTheme

private val Indigo = Color(0xFF3949AB)
private val Fondo = Color(0xFFF6F7FB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)
private val Borde = Color(0xFFE3E6F0)
private val Lavanda = Color(0xFFE8EAF6)

// Datos de ejemplo. Más adelante esto vendrá de la base de datos.
data class ApunteItem(
    val titulo: String,
    val materia: String,
    val fecha: String
)

private val apuntesEjemplo = listOf(
    ApunteItem("Garantías constitucionales", "Derecho Constitucional", "Hoy"),
    ApunteItem("Despido: causales", "Derecho Laboral", "Ayer"),
    ApunteItem("Prescripción de la acción penal", "Derecho Penal", "5 oct")
)

private const val TODAS = "Todas"

@Composable
fun Apuntes(
    apuntes: List<ApunteItem> = apuntesEjemplo,
    onVer: (ApunteItem) -> Unit = {},
    onEditar: (ApunteItem) -> Unit = {},
    onCompartir: (ApunteItem) -> Unit = {},
    onNuevoApunte: () -> Unit = {}
) {
    var filtro by remember { mutableStateOf(TODAS) }
    val materias = listOf(TODAS) + apuntes.map { it.materia }.distinct()
    val visibles = if (filtro == TODAS) apuntes else apuntes.filter { it.materia == filtro }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)) {
            Text(text = "${apuntes.size} apuntes guardados", fontSize = 12.sp, color = TextoSuave)
            Text(
                text = "Apuntes",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(materias) { materia ->
                FilterChip(
                    selected = filtro == materia,
                    onClick = { filtro = materia },
                    label = { Text(materia) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibles) { apunte ->
                TarjetaApunte(apunte, onVer, onEditar, onCompartir)
            }
        }

        Button(
            onClick = onNuevoApunte,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(48.dp)
        ) {
            Text(text = "+ Nuevo apunte", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TarjetaApunte(
    apunte: ApunteItem,
    onVer: (ApunteItem) -> Unit,
    onEditar: (ApunteItem) -> Unit,
    onCompartir: (ApunteItem) -> Unit
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
                Text(text = "${apunte.materia} · ${apunte.fecha}", fontSize = 12.sp, color = TextoSuave)
                Text(
                    text = apunte.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonAccion("Ver", Modifier.weight(1f)) { onVer(apunte) }
                BotonAccion("Editar", Modifier.weight(1f)) { onEditar(apunte) }
                BotonAccion("Compartir", Modifier.weight(1f)) { onCompartir(apunte) }
            }
        }
    }
}

@Composable
private fun BotonAccion(texto: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFC5CAE9)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
        modifier = modifier.height(44.dp)
    ) {
        Text(text = texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF283593))
    }
}

@Preview(showBackground = true)
@Composable
fun ApuntesPreview() {
    StudyDeskTheme {
        Apuntes()
    }
}