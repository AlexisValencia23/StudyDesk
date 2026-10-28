package com.example.studydesk.model

// Un material de estudio: un PDF guardado dentro de la app (carpeta assets)
data class MaterialEstudio(
    val materia: String,
    val titulo: String,
    val descripcion: String,
    val archivoPdf: String,
    val fechaActualizacion: String
)

// Materiales disponibles en la app. Cada archivo debe estar en app/src/main/assets.
// La fecha de actualización se escribe a mano aquí cada vez que se cambia un PDF.
val materialesEstudio = listOf(
    MaterialEstudio(
        materia = "Derecho Civil",
        titulo = "Código Civil",
        descripcion = "Texto del Código Civil para consulta y estudio.",
        archivoPdf = "codigo_civil.pdf",
        fechaActualizacion = "27 oct 2026"
    ),
    MaterialEstudio(
        materia = "Derecho Penal",
        titulo = "Código Penal",
        descripcion = "Texto del Código Penal para consulta y estudio.",
        archivoPdf = "codigo_penal.pdf",
        fechaActualizacion = "27 oct 2026"
    ),
    MaterialEstudio(
        materia = "Derecho Constitucional",
        titulo = "Material de Derecho Constitucional",
        descripcion = "Material de estudio sobre Derecho Constitucional.",
        archivoPdf = "derecho_constitucional.pdf",
        fechaActualizacion = "27 oct 2026"
    )
)