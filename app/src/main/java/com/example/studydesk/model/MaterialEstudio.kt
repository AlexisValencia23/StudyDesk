package com.example.studydesk.model

// Un material de estudio: un PDF guardado dentro de la app (carpeta assets)
data class MaterialEstudio(
    val materia: String,
    val titulo: String,
    val descripcion: String,
    val archivoPdf: String
)

// Materiales disponibles en la app. Cada archivo debe estar en app/src/main/assets
val materialesEstudio = listOf(
    MaterialEstudio(
        materia = "Derecho Civil",
        titulo = "Código Civil",
        descripcion = "Texto del Código Civil para consulta y estudio.",
        archivoPdf = "codigo_civil.pdf"
    ),
    MaterialEstudio(
        materia = "Derecho Penal",
        titulo = "Código Penal",
        descripcion = "Texto del Código Penal para consulta y estudio.",
        archivoPdf = "codigo_penal.pdf"
    ),
    MaterialEstudio(
        materia = "Derecho Constitucional",
        titulo = "Material de Derecho Constitucional",
        descripcion = "Material de estudio sobre Derecho Constitucional.",
        archivoPdf = "derecho_constitucional.pdf"
    )
)