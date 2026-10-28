package com.example.studydesk.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

// Abre un PDF guardado en assets y dibuja sus páginas como imágenes.
// Hay que crearlo fuera del hilo principal, porque la primera vez copia el archivo.
class LectorPdf(context: Context, nombreArchivo: String) {

    private val descriptor: ParcelFileDescriptor
    private val renderizador: PdfRenderer

    // PdfRenderer solo permite tener una página abierta a la vez
    private val turno = Mutex()

    val totalPaginas: Int

    init {
        // PdfRenderer necesita un archivo real, así que se copia desde assets al almacenamiento de la app
        val archivo = File(context.cacheDir, nombreArchivo)
        if (!archivo.exists()) {
            context.assets.open(nombreArchivo).use { entrada ->
                archivo.outputStream().use { salida -> entrada.copyTo(salida) }
            }
        }
        descriptor = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
        renderizador = PdfRenderer(descriptor)
        totalPaginas = renderizador.pageCount
    }

    suspend fun dibujarPagina(indice: Int, anchoPx: Int): Bitmap = turno.withLock {
        withContext(Dispatchers.IO) {
            renderizador.openPage(indice).use { pagina ->
                val altoPx = (anchoPx.toFloat() * pagina.height / pagina.width).toInt()
                val imagen = Bitmap.createBitmap(anchoPx, altoPx, Bitmap.Config.ARGB_8888)
                imagen.eraseColor(Color.WHITE)
                pagina.render(imagen, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                imagen
            }
        }
    }

    fun cerrar() {
        renderizador.close()
        descriptor.close()
    }
}