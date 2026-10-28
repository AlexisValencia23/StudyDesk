# StudyDesk

Aplicación móvil para Android que ayuda a estudiantes de Derecho (y abogados) a organizar su estudio: llevar el control de entregas, guardar apuntes y leer material de estudio, todo desde el celular.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**, Duoc UC.

## Integrantes

- Paolo Muñoz
- Benjamín Farias
- Alexis Valencia

## Qué puede hacer la app

- **Registro e inicio de sesión:** el usuario crea una cuenta (nombre, correo, contraseña y perfil: estudiante de Derecho o abogado/a) e inicia sesión. Los datos se validan y se guardan en el celular. También puede cerrar sesión desde Inicio.
- **Inicio:** resumen con las últimas entregas, los apuntes recientes, acceso al material de estudio y un botón que simula un aviso de cambio normativo.
- **Entregas:** lista de trabajos con materia, nombre y fecha. Se agregan con un formulario que usa un calendario para elegir la fecha. El botón Calendario muestra solo las entregas de un día.
- **Apuntes:** se pueden crear, ver, editar y compartir (por WhatsApp, correo u otra app). Tienen filtro por materia.
- **Material de estudio:** tarjetas por materia (Derecho Civil, Penal y Constitucional) con buscador.
    - **Abrir material:** muestra el PDF dentro de la app.
    - **Última actualización:** fecha escrita a mano por el equipo cada vez que se cambia un PDF. El usuario solo la ve.
    - **Comentarios:** cada usuario puede comentar un material, y editar o eliminar solo sus propios comentarios.
- **Avisos de cambios normativos:** notificación de ejemplo que simula el aviso de una modificación en una norma.

## Requisitos de la evaluación

| Requisito | Dónde está |
|---|---|
| Formulario con validación | Login, Registro, Nueva entrega, Nuevo apunte y Comentarios |
| Imagen | Logo en la pantalla de Login |
| Animación | Las tarjetas de Inicio aparecen una tras otra |
| Modal | «Cuenta creada», formularios de entregas y apuntes, calendario, comentarios y confirmación al eliminar |
| Persistencia con Room | Usuarios, entregas, apuntes y comentarios |
| Navegación entre vistas | Navigation Compose con barra inferior y visor de PDF |
| Recurso nativo | Notificaciones y selector nativo para compartir |
| Git y Trello | Commits frecuentes y tablero de tareas del proyecto |

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Room 3 (base de datos local) con KSP
- Navigation Compose
- PdfRenderer de Android (visor de PDF)
- Arquitectura MVVM

## Estructura del proyecto

```
com.example.studydesk
├── model          Tablas y consultas de la base de datos, y la lista de materiales
├── repository     Acceso a los datos
├── viewmodel      Lógica de cada función (usuarios, entregas, apuntes y comentarios)
├── notification   Notificaciones de la app
├── pdf            Lectura de los PDF
├── util           Funciones compartidas (fecha de hoy)
├── ui             Pantallas y navegación
│   └── theme      Colores y tipografía
└── MainActivity   Crea la base de datos y arranca la app
```

Los PDF del material de estudio están en `app/src/main/assets`.

## Cómo ejecutar el proyecto

1. Abrir la carpeta del proyecto con Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Elegir un emulador (o un celular conectado con depuración USB) y presionar **Run**.
4. Aceptar el permiso de notificaciones cuando la app lo pida.

Requiere Android 7.0 (API 24) o superior.

Si se cambian las tablas de la base de datos, hay que desinstalar la app del emulador antes de volver a ejecutarla.

## Cómo agregar o actualizar un material de estudio

1. Copiar el PDF a `app/src/main/assets`, con un nombre en minúscula y sin espacios (por ejemplo `codigo_civil.pdf`).
2. Abrir `model/MaterialEstudio.kt` y agregar o editar el material en la lista `materialesEstudio`, incluyendo la `fechaActualizacion`.

## Notas

- Es un proyecto académico (MVP). La contraseña se guarda sin cifrar, algo que una app real no haría.
- El aviso de cambios normativos es de ejemplo.
- Los PDF de legislación son de uso académico y no reemplazan las fuentes oficiales.

## Mejoras futuras

- Recuperación de contraseña.
- Cifrado de contraseñas.
- Zoom con los dedos en el visor de PDF.