
package com.example.studydesk.util

import android.content.Context
import java.util.UUID

class IdentificadorSync(context: Context) {

    private val preferencias = context.applicationContext
        .getSharedPreferences(
            "studydesk_identificadores",
            Context.MODE_PRIVATE
        )

    // Identificador unico de esta instalacion.
    // Se conserva al cerrar y volver a abrir StudyDesk.
    private val instalacionId: String = synchronized(bloqueo) {

        preferencias.getString("instalacion_id", null)
            ?: UUID.randomUUID().toString().also { nuevoId ->

                val guardado = preferencias.edit()
                    .putString("instalacion_id", nuevoId)
                    .commit()

                check(guardado) {
                    "No se pudo guardar el ID de instalacion"
                }
            }
    }

    // Identificador estable de un apunte local
    fun apunte(idLocal: Int): String {
        require(idLocal > 0)
        return "A-$instalacionId-$idLocal"
    }

    // Identificador estable de una entrega local
    fun entrega(idLocal: Int): String {
        require(idLocal > 0)
        return "E-$instalacionId-$idLocal"
    }

    companion object {
        private val bloqueo = Any()
    }
}
