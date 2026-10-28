package com.example.studydesk.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.pdf.LectorPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val TextoOscuro = Color(0xFF1B1F3B)
private val FondoVisor = Color(0xFFE3E6F0)

// Muestra un PDF de la carpeta assets, página por página, con una barra para volver
@Composable
fun VisorPdf(
    titulo: String,
    nombreArchivo: String,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var hayError by remember { mutableStateOf(false) }

    // Abre el PDF en segundo plano y lo cierra cuando se sale de la pantalla
    val lector by produceState<LectorPdf?>(initialValue = null, key1 = nombreArchivo) {
        val abierto = withContext(Dispatchers.IO) {
            runCatching { LectorPdf(context, nombreArchivo) }.getOrNull()
        }
        if (abierto == null) hayError = true
        value = abierto
        awaitDispose { abierto?.cerrar() }
    }

    val anchoPantallaPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.roundToPx()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoVisor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextoOscuro
                )
            }
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }

        val lectorListo = lector
        when {
            hayError -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No se pudo abrir el documento.", color = TextoOscuro)
            }
            lectorListo == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = lectorListo.totalPaginas) { indice ->
                    PaginaPdf(lectorListo, indice, anchoPantallaPx)
                }
            }
        }
    }
}

// Dibuja una página; mientras se prepara muestra un círculo de carga
@Composable
private fun PaginaPdf(lector: LectorPdf, indice: Int, anchoPx: Int) {
    val pagina by produceState<ImageBitmap?>(initialValue = null, key1 = indice) {
        value = lector.dibujarPagina(indice, anchoPx).asImageBitmap()
    }

    val imagen = pagina
    if (imagen == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Image(
            bitmap = imagen,
            contentDescription = "Página ${indice + 1}",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}