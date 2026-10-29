# StudyDesk

**Aplicación móvil de estudio y organización para estudiantes de Derecho y profesionales del área jurídica.**

Proyecto desarrollado para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)** de **Duoc UC**, correspondiente al caso 13.

StudyDesk permite organizar entregas académicas, crear apuntes personales, consultar material de legislación chilena en PDF y compartir comentarios sobre los documentos de estudio.

## Integrantes

- Paolo Muñoz
- Benjamín Farias
- Alexis Valencia

## Funcionalidades principales

### 1. Registro e inicio de sesión

- Registro de usuarios con nombre, correo, contraseña y perfil.
- Perfiles disponibles: Estudiante de Derecho y Abogado/a.
- Validación de campos obligatorios, formato de correo y longitud mínima de contraseña.
- Comprobación de correos electrónicos ya registrados.
- Inicio y cierre de sesión.
- Conservación de la sesión mediante SharedPreferences.
- Identificación de cada cuenta mediante un ID único generado por Room.

Cada usuario puede tener el mismo nombre que otra persona sin que eso afecte la identificación de su cuenta.

### 2. Pantalla de Inicio

- Bienvenida personalizada con el nombre del usuario.
- Resumen de las últimas entregas registradas.
- Visualización de apuntes recientes.
- Acceso directo al material de estudio.
- Botón de demostración para simular una notificación de cambio normativo.
- Animaciones de aparición en las tarjetas de información.

Los resúmenes de entregas y apuntes corresponden únicamente a la cuenta que inició sesión.

### 3. Gestión de entregas

- Crear entregas con materia, título y fecha.
- Seleccionar fechas utilizando un calendario.
- Consultar todas las entregas registradas.
- Filtrar entregas según una fecha seleccionada.
- Eliminar entregas con una ventana de confirmación.
- Validar los campos antes de guardar.
- Guardar las entregas en Room.

**Separación por usuario:** cada entrega se asocia al ID de su propietario. Una cuenta no puede ver ni eliminar las entregas de otra.

### 4. Gestión de apuntes

- Crear apuntes indicando materia, título y contenido.
- Consultar el contenido completo de un apunte.
- Editar apuntes existentes.
- Eliminar apuntes con confirmación.
- Filtrar apuntes por materia.
- Compartir apuntes mediante aplicaciones disponibles en Android, como correo electrónico o WhatsApp.
- Validar los campos antes de guardar.
- Mantener los apuntes almacenados con Room.

**Separación por usuario:** cada apunte pertenece a una cuenta específica. Los usuarios únicamente pueden visualizar, editar y eliminar sus propios apuntes.

### 5. Material de estudio

La aplicación incluye documentos en formato PDF para consulta académica.

| Materia | Documento |
|---|---|
| Derecho Civil | Código Civil |
| Derecho Penal | Código Penal |
| Derecho Constitucional | Material de Derecho Constitucional |

Funciones disponibles:

- Buscar documentos por materia, título o descripción.
- Abrir y leer archivos PDF dentro de la aplicación.
- Desplazarse entre las páginas del documento.
- Consultar la fecha de actualización registrada para cada material.
- Abrir una sección de comentarios asociada a cada documento.

Los PDF están almacenados localmente en la carpeta `assets` y se visualizan mediante `PdfRenderer` de Android.

### 6. Comentarios del material de estudio

- Publicar comentarios asociados a un material.
- Leer los comentarios realizados por otros usuarios.
- Editar comentarios propios.
- Eliminar comentarios propios con confirmación.
- Validar que el contenido no esté vacío.
- Guardar los comentarios mediante Room.

**Control de permisos:** cada comentario guarda el nombre visible de su autor y el ID de la cuenta que lo creó.

Los botones para editar o eliminar aparecen únicamente en los comentarios propios. Además, las operaciones en Room verifican el ID del autor antes de modificar o eliminar un comentario.

Esto evita que dos usuarios con el mismo nombre puedan modificar los comentarios del otro.

Los comentarios se comparten entre las cuentas registradas en la misma instalación de la aplicación. No existe sincronización entre diferentes dispositivos.

### 7. Notificaciones

StudyDesk incorpora una demostración del sistema de notificaciones de Android.

Desde Inicio, el usuario puede activar un aviso simulado de modificación normativa.

Se utiliza `NotificationManager` y se solicita el permiso correspondiente en Android 13 o superior.

## Requisitos de la evaluación

| Requisito | Implementación en StudyDesk |
|---|---|
| Formulario | Registro, Login, Entregas, Apuntes y Comentarios |
| Validación de campos | Clase `Validaciones.kt` y mensajes visuales |
| Imagen | Logo de StudyDesk en Login |
| Animación | Aparición de tarjetas en Inicio y animación de carga del Login |
| Modal | Cuenta creada, formularios, calendario, comentarios y confirmaciones |
| Persistencia con Room | Usuarios, apuntes, entregas y comentarios |
| Navegación entre vistas | Navigation Compose y barra de navegación inferior |
| Recurso nativo | Notificaciones de Android y selector para compartir |
| Git | Control de versiones y registro de cambios en GitHub |
| Trello | Organización y seguimiento de tareas del equipo |

## Tecnologías utilizadas

- **Kotlin:** lenguaje principal de programación.
- **Android Studio:** entorno de desarrollo.
- **Jetpack Compose:** construcción de las interfaces.
- **Material Design 3:** componentes visuales.
- **Navigation Compose:** navegación entre pantallas.
- **Room 3:** persistencia de datos en una base de datos local.
- **KSP:** procesamiento de anotaciones de Room.
- **SQLite:** almacenamiento local de la información.
- **SharedPreferences:** almacenamiento de la sesión del usuario.
- **PdfRenderer:** visualización de documentos PDF.
- **Coroutines y Flow:** operaciones asíncronas y actualización de información.
- **Git y GitHub:** control de versiones.
- **Trello:** organización del proyecto.

## Arquitectura del proyecto

El proyecto utiliza una organización basada en el patrón **MVVM (Model - View - ViewModel)**, junto con una capa Repository para acceder a la base de datos.

| Carpeta o archivo | Responsabilidad |
|---|---|
| `model/` | Entidades de Room, DAO, base de datos y materiales de estudio |
| `repository/` | Operaciones de acceso a los datos |
| `viewmodel/` | Lógica de usuarios, entregas, apuntes y comentarios |
| `ui/` | Pantallas y componentes visuales |
| `ui/theme/` | Colores y configuración visual |
| `notification/` | Notificaciones de Android |
| `pdf/` | Lectura y renderización de archivos PDF |
| `util/` | Validaciones, fechas y gestión de sesión |
| `MainActivity.kt` | Inicialización de Room y de la aplicación |

La base de datos contiene cuatro entidades principales: `Usuario`, `Apunte`, `Entrega` y `Comentario`.

Los apuntes y las entregas se relacionan con su propietario mediante `usuarioId`, mientras que los comentarios utilizan `autorId` para controlar los permisos de modificación.

## Cómo ejecutar StudyDesk

**Requisitos:**

- Android Studio.
- SDK de Android configurado.
- Emulador o dispositivo físico con Android 7.0 (API 24) o superior.
- Conexión a Internet para descargar las dependencias durante la configuración inicial.

**Pasos:**

1. Clonar o descargar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Esperar a que finalice la sincronización de Gradle.
4. Seleccionar un emulador o dispositivo Android.
5. Ejecutar la aplicación mediante **Run**.
6. Registrar una cuenta para comenzar a utilizar StudyDesk.
7. Conceder el permiso de notificaciones cuando Android lo solicite, si corresponde.

## Comprobación de funcionalidades

Para comprobar la separación de usuarios:

1. Registrar una cuenta A y crear un apunte y una entrega.
2. Cerrar sesión y registrar o iniciar sesión con una cuenta B.
3. Verificar que la cuenta B no pueda visualizar los registros de A.
4. Crear nuevos registros desde B.
5. Volver a la cuenta A y comprobar que conserva únicamente sus propios registros.

Para comprobar los comentarios:

1. Publicar un comentario desde la cuenta A.
2. Entrar con la cuenta B.
3. Verificar que B pueda leer el comentario, pero no editarlo ni eliminarlo.
4. Publicar un comentario desde B.
5. Confirmar que cada cuenta puede modificar exclusivamente sus propios comentarios.

## Material de estudio y actualización

Los documentos se encuentran en:

`app/src/main/assets/`

Para agregar o actualizar un documento:

1. Incorporar el archivo PDF a la carpeta `assets`.
2. Registrar o modificar su información en `model/MaterialEstudio.kt`.
3. Actualizar manualmente el campo `fechaActualizacion`.

Actualmente, los materiales y sus fechas de actualización son gestionados manualmente por el equipo.

## Consideraciones del proyecto

StudyDesk es un prototipo académico.

- Los datos se almacenan localmente en el dispositivo.
- La aplicación no utiliza un servidor ni sincronización en la nube.
- Las contraseñas se almacenan sin cifrar, por lo que el sistema de autenticación no está preparado para un entorno de producción.
- Los avisos de cambios normativos son simulados.
- La actualización de los PDF se realiza manualmente.
- La versión actual de Room no incorpora migraciones de base de datos. Durante el desarrollo, los cambios de estructura pueden requerir borrar los datos de la aplicación.
- El material jurídico se incluye con fines académicos y debe contrastarse con las fuentes oficiales cuando se necesite consultar legislación vigente.

## Mejoras futuras

Como posibles mejoras posteriores al proyecto académico se consideran:

- Almacenamiento seguro de contraseñas.
- Recuperación de cuentas.
- Sincronización de información entre dispositivos.
- Actualización automática de documentos normativos.
- Zoom mediante gestos en el lector de PDF.

Estas mejoras no forman parte de las funcionalidades implementadas en la versión actual.

---

**StudyDesk — Proyecto académico de Desarrollo de Aplicaciones Móviles, Duoc UC.**
