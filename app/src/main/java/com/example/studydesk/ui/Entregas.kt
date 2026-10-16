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
data class EntregaItem(
    val materia: String,
    val titulo: String,
    val fecha: String
)

private val entregasEjemplo = listOf(
    EntregaItem("Derecho Civil", "Ensayo: responsabilidad contractual", "12 oct"),
    EntregaItem("Derecho Penal", "Análisis de jurisprudencia", "15 oct"),
    EntregaItem("Derecho Constitucional", "Resumen: control de constitucionalidad", "20 oct"),
    EntregaItem("Derecho Laboral", "Caso: término de contrato", "28 oct")
)

@Composable
fun Entregas(
    entregas: List<EntregaItem> = entregasEjemplo,
    onAgregarEntrega: () -> Unit = {},
    onCalendario: () -> Unit = {}
) {
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
                onClick = onAgregarEntrega,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "+ Agregar entrega", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TarjetaEntrega(entrega: EntregaItem) {
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

@Preview(showBackground = true)
@Composable
fun EntregasPreview() {
    StudyDeskTheme {
        Entregas()
    }
}