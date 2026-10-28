package com.example.studydesk.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FechaUtil {

    // Fecha de hoy en formato corto, por ejemplo "27 oct"
    fun hoy(): String {
        return SimpleDateFormat("d MMM", Locale.forLanguageTag("es-CL")).format(Date())
    }
}