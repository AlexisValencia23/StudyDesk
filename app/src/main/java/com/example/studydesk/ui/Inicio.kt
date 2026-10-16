package com.example.studydesk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun Inicio(
    onIrAMaterial: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text(text = "Jueves 15 de octubre", fontSize = 12.sp, color = TextoSuave)
            Text(
                text = "Hola, 'Nombre'",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        Tarjeta {
            Titulo("Próximas entregas")
            FilaItem("Ensayo: responsabilidad contractual", "Derecho Civil", "12 oct")
            HorizontalDivider(color = Borde)
            FilaItem("Análisis de jurisprudencia", "Derecho Penal", "15 oct")
        }

        Tarjeta {
            Titulo("Apuntes recientes")
            FilaItem("Garantías constitucionales", "Derecho Constitucional", "Hoy")
            HorizontalDivider(color = Borde)
            FilaItem("Despido: causales", "Derecho Laboral", "Ayer")
        }

        Tarjeta(fondo = Lavanda, borde = Color(0xFFC5CAE9)) {
            Titulo("Material de estudio")
            Text(
                text = "4 materias con normas y jurisprudencia actualizadas.",
                fontSize = 12.sp,
                color = Color(0xFF283593)
            )
            Button(
                onClick = onIrAMaterial,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Ir al material de estudio", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Tarjeta(
    fondo: Color = Color.White,
    borde: Color = Borde,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(1.dp, borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = contenido
        )
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(text = texto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
}

@Composable
private fun FilaItem(titulo: String, materia: String, fecha: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
            Text(text = materia, fontSize = 12.sp, color = TextoSuave)
        }
        Surface(shape = RoundedCornerShape(8.dp), color = Lavanda) {
            Text(
                text = fecha,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF283593),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InicioPreview() {
    StudyDeskTheme {
        Inicio()
    }
}
