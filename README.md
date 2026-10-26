# StudyDesk

Aplicación móvil para Android que ayuda a estudiantes de Derecho (y abogados) a organizar su estudio: llevar el control de entregas, guardar apuntes y consultar material de estudio, todo desde el celular.

Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)**, Duoc UC.

## Integrantes

- Paolo Muñoz
- Benjamín Farias
- Alexis Valencia

## Qué puede hacer la app

- **Registro e inicio de sesión:** el usuario crea una cuenta (nombre, correo, contraseña y perfil: estudiante de Derecho o abogado/a) y luego inicia sesión. Los datos se validan y se guardan en el celular.
- **Inicio:** resumen con las últimas entregas, los apuntes recientes y accesos rápidos.
- **Entregas:** lista de trabajos con materia, nombre y fecha. Se pueden agregar con un formulario y un calendario para elegir la fecha, y el botón Calendario muestra solo las entregas de un día.
- **Apuntes:** se pueden crear, ver, editar y compartir (por WhatsApp, correo u otra app). Tienen filtro por materia.
- **Material de estudio:** documentos organizados por materia, con buscador. *(En desarrollo: por ahora muestra datos de ejemplo.)*
- **Avisos de cambios normativos:** notificación de ejemplo que simula el aviso de una modificación en una norma.

## Requisitos de la evaluación

| Requisito | Dónde está |
|---|---|
| Formulario con validación | Login, Registro, Nueva entrega y Nuevo apunte |
| Imagen | Logo en la pantalla de Login |
| Animación | Las tarjetas de Inicio aparecen una tras otra |
| Modal | «Cuenta creada», formularios de entregas y apuntes, y vista de apunte |
| Persistencia con Room | Usuarios, entregas y apuntes |
| Navegación entre vistas | Navigation Compose con barra inferior |
| Recurso nativo | Notificaciones y selector nativo para compartir |
| Git y Trello | Commits frecuentes y tablero de tareas del proyecto |

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Room 3 (base de datos local) con KSP
- Navigation Compose
- Arquitectura MVVM

## Estructura del proyecto

```
com.example.studydesk
├── model          Tablas y consultas de la base de datos (Usuario, Entrega, Apunte)
├── repository     Acceso a los datos
├── viewmodel      Lógica de cada función (usuarios, entregas, apuntes)
├── notification   Notificaciones de la app
├── ui             Pantallas y navegación
│   └── theme      Colores y tipografía
└── MainActivity   Crea la base de datos y arranca la app
```

## Cómo ejecutar el proyecto

1. Abrir la carpeta del proyecto con Android Studio.
2. Esperar a que termine la sincronización de Gradle.
3. Elegir un emulador (o un celular conectado con depuración USB) y presionar **Run**.
4. Aceptar el permiso de notificaciones cuando la app lo pida.

Requiere Android 7.0 (API 24) o superior.

Si se cambian las tablas de la base de datos, hay que desinstalar la app del emulador antes de volver a ejecutarla.

## Notas

- Es un proyecto académico (MVP). La contraseña se guarda sin cifrar, algo que una app real no haría.
- Los datos del material de estudio y el aviso de cambios normativos son de ejemplo.

## Mejoras futuras

- Recuperación de contraseña con envío de correo.
- Conexión del material de estudio con una fuente oficial de legislación.
- Cifrado de contraseñas.
