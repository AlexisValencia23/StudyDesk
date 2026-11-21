# StudyDesk

**Aplicación Android para organizar entregas y apuntes, consultar material jurídico y compartir comentarios.**  
Proyecto académico de **Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC — caso 13**.

## Guía rápida para mostrar el proyecto al profesor

> **Importante:** los siguientes comandos están pensados para **Windows PowerShell**, con el proyecto ya descargado. **Ejecuta Spring Boot desde `studydesk-backend`** para abrir la base H2 correcta. No borres la carpeta `data`.

### 1. Encender Spring Boot y habilitar la consola H2

Abre PowerShell desde la carpeta principal de StudyDesk y ejecuta:

```powershell
cd C:\Users\paolo\StudioProjects\StudyDesk\studydesk-backend
$env:SPRING_H2_CONSOLE_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

Deja esta terminal **abierta** durante la demostración. Spring Boot se ejecuta en `http://localhost:8080` y utiliza **Java 21**. La variable de entorno habilita la consola web H2 durante esa ejecución, sin cambiar la configuración guardada del proyecto. La dependencia `spring-boot-h2console` ya está incluida en `pom.xml`.

Desde una **segunda terminal**, comprueba que el servidor está escuchando:

```powershell
Test-NetConnection localhost -Port 8080
```

Busca `TcpTestSucceeded : True`.

### 2. Ver las tablas y ejecutar consultas SQL

Con el servidor iniciado y la consola habilitada, abre:

**http://localhost:8080/h2-console**

En el formulario de conexión utiliza:

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/studydesk` |
| User Name | `sa` |
| Password | *(vacío)* |

Presiona **Connect**. A continuación, puedes copiar estas consultas SQL para enseñar los datos reales del servidor:

```sql
-- Entregas, de la más reciente a la más antigua
SELECT ID, TITULO, MATERIA, FECHA, CORREO_USUARIO, ID_CLIENTE
FROM ENTREGAS
ORDER BY ID DESC;

-- Apuntes guardados
SELECT ID, TITULO, MATERIA, CONTENIDO, FECHA, CORREO_USUARIO, ID_CLIENTE
FROM APUNTES
ORDER BY ID DESC;

-- Cantidad total de registros
SELECT COUNT(*) AS TOTAL_ENTREGAS FROM ENTREGAS;
SELECT COUNT(*) AS TOTAL_APUNTES FROM APUNTES;
```

Para filtrar las entregas de una cuenta de ejemplo, **reemplaza el correo**:

```sql
SELECT ID, TITULO, MATERIA, FECHA, ID_CLIENTE
FROM ENTREGAS
WHERE LOWER(CORREO_USUARIO) = LOWER('alumno@example.com')
ORDER BY ID DESC;
```

**No uses `DROP`, `DELETE` ni `UPDATE` para esta demostración:** basta con `SELECT`. Si las tablas salen vacías, revisa que el servidor se haya iniciado en la carpeta `studydesk-backend` y que la URL JDBC sea la indicada.

**Nota sobre H2:** si `/h2-console` no abre, comprueba los mensajes de inicio de Spring Boot. La consola es una función de desarrollo; mantenerla habilitada únicamente para la presentación.

### 3. Consultar directamente la API REST con PowerShell

Con Spring Boot encendido, desde otra terminal:

```powershell
$correo = "alumno@example.com"  # Reemplazar por la cuenta usada en la demo
$correoUrl = [uri]::EscapeDataString($correo)

Invoke-RestMethod "http://localhost:8080/api/apuntes/usuario/correo/$correoUrl" |
    Format-Table id, titulo, materia, fecha, correoUsuario, idCliente

Invoke-RestMethod "http://localhost:8080/api/entregas/usuario/correo/$correoUrl" |
    Format-Table id, titulo, materia, fecha, correoUsuario, idCliente
```

También puedes abrir rutas en el navegador:

- `http://localhost:8080/api/apuntes/usuario/correo/alumno@example.com`
- `http://localhost:8080/api/entregas/usuario/correo/alumno@example.com`
- `http://localhost:8080/api/apuntes/11` *(ID de ejemplo)*
- `http://localhost:8080/api/entregas/46` *(ID de ejemplo)*

Los IDs pueden ser distintos en cada instalación. Consultar primero los IDs reales desde la consola H2.

### 4. Ejecutar la aplicación en Android Studio

1. Abrir la carpeta principal de StudyDesk en **Android Studio**.
2. Esperar a que finalice la sincronización de Gradle.
3. Iniciar un **emulador Android** y presionar **Run**.
4. Registrar una cuenta o iniciar sesión.
5. Crear un apunte y una entrega.
6. Entrar en la pestaña **Servidor** de StudyDesk para consultar la API.
7. Volver a H2 y repetir las consultas `SELECT` para ver los registros creados.

**Direcciones distintas:**

- Navegador de Windows: `http://localhost:8080/`.
- Emulador Android: `http://10.0.2.2:8080/`, configurado en `RetrofitClient.kt`.

`10.0.2.2` funciona para el emulador Android, **no directamente para un teléfono físico**. Un teléfono real requiere configurar la IP local del computador y los permisos de red correspondientes.

### 5. Mostrar la sincronización automática sin conexión

1. Con la aplicación abierta y la sesión iniciada, **detén Spring Boot** mediante `Ctrl+C`.
2. Crea **un apunte y una entrega**. Ambos deben guardarse en Room, aunque falle el envío remoto.
3. Vuelve a iniciar Spring Boot con el comando del apartado 1.
4. Trae StudyDesk al primer plano o vuelve a abrirla.
5. Revisa la pestaña **Servidor** y las tablas H2: ahora deben existir los registros creados sin conexión.
6. Vuelve a sincronizar: **no deben generarse duplicados**.

En **Logcat**, filtra por la etiqueta `StudyDeskSync`. Los errores `ConnectException` son esperados mientras el servidor esté apagado; lo importante es que la recuperación posterior enlace los registros de Room con Spring Boot:

```text
StudyDeskSync: Pendiente vinculado. Room: 5, Spring Boot: 11
StudyDeskSync: Entrega pendiente vinculada. Room: 7, Spring Boot: 46
```

Estos IDs son ejemplos de una prueba realizada; pueden variar.

### 6. Ejecutar las pruebas automatizadas

Desde la carpeta `studydesk-backend`:

```powershell
.\mvnw.cmd clean test
```

Resultado esperado: **`BUILD SUCCESS`**. Las pruebas de integración usan una **base H2 temporal**, distinta de los datos persistentes de la demostración.

### 7. Apagar el servidor al terminar

En la terminal de Spring Boot presiona **`Ctrl+C`**. No elimines archivos de `studydesk-backend/data/`: ahí se guarda la base de datos H2 persistente.

---

## Integrantes

- Paolo Muñoz
- Benjamín Farias
- Alexis Valencia

## Funcionalidades de StudyDesk

### Registro e inicio de sesión

- Registro con nombre, correo, contraseña y perfil (Estudiante de Derecho o Abogado/a).
- Validación de campos, formato de correo y correos ya registrados.
- Inicio y cierre de sesión, conservando la sesión con `SharedPreferences`.
- Cuentas identificadas por su ID de usuario en Room, aunque tengan el mismo nombre.

### Pantalla Inicio

- Bienvenida personalizada, últimas entregas y apuntes recientes.
- Acceso al material de estudio.
- Animaciones de interfaz y notificación demostrativa de cambio normativo.

### Entregas

- Crear entregas por materia, título y fecha.
- Usar un calendario, consultar y filtrar entregas.
- Eliminar entregas mediante confirmación.
- Guardar en Room y sincronizar con Spring Boot cuando haya conexión.
- Recuperar automáticamente las **creaciones pendientes** después de una desconexión.

### Apuntes

- Crear, consultar, editar y eliminar apuntes.
- Filtrar por materia y compartir mediante aplicaciones de Android.
- Guardar en Room y crear/editar/eliminar registros remotos con conexión.
- Recuperar automáticamente las **creaciones pendientes** cuando regresa el servidor.

### Material jurídico y comentarios

- Consultar documentos PDF de Derecho Civil, Penal y Constitucional.
- Buscar materiales por título, materia o descripción.
- Leer PDF locales mediante `PdfRenderer`.
- Publicar, editar y eliminar comentarios asociados a documentos.
- Restringir la edición y eliminación de comentarios a su autor.
- Los PDF se incluyen en `app/src/main/assets/` y se actualizan manualmente.
- Los comentarios están en Room y **no** se sincronizan con Spring Boot.

### Notificaciones y pantalla Servidor

- Notificación nativa demostrativa con permiso en Android 13 o superior.
- Pantalla **Servidor** para consultar los apuntes y las entregas remotos, actualizar listas y ejecutar sincronización manual de pendientes.
- Además, la comprobación de pendientes se ejecuta automáticamente al regresar a la aplicación.

## Arquitectura y bases de datos

La aplicación sigue una organización **MVVM (Model–View–ViewModel)** con repositorios, Jetpack Compose y Room.

| Componente | Responsabilidad |
|---|---|
| `ui/` | Pantallas y navegación con Compose |
| `viewmodel/` | Estado y operaciones de cada pantalla |
| `repository/` | Acceso a Room y a Retrofit |
| `model/` | Entidades y DAO locales |
| `network/` | API REST, Retrofit y modelos de transporte |
| `util/` | Validaciones, fechas, sesión e identificadores de sincronización |
| `notification/` | Notificaciones Android |
| `pdf/` | Lectura de PDF |
| `studydesk-backend/` | API Java con Spring Boot, JPA y H2 |

### Diferencia entre Room y H2

| Información | Room (Android) | H2 (backend) |
|---|---|---|
| Usuarios | Sí | No: autenticación de la app es local |
| Apuntes | Sí | Sí, copia sincronizada |
| Entregas | Sí | Sí, copia sincronizada |
| Comentarios | Sí | No |
| Archivos PDF | `assets/` | No |

**Room y H2 son bases distintas.** Room conserva la información local en Android; H2 conserva los apuntes y las entregas asociados a correos en el servidor.

Configuración actual del backend, en `studydesk-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/studydesk
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

**Importante:** `studydesk-backend/data/` está excluida de Git, de modo que una copia nueva del repositorio empezará con otra base H2 hasta que se creen sus propios registros.

### Prevención de duplicados

Cada apunte o entrega nueva obtiene un identificador estable llamado **`idCliente`**, separado del ID de Room y del ID remoto. Si Android repite la misma creación porque perdió la respuesta de red, Spring Boot devuelve el registro ya creado. Si el mismo `idCliente` se reutiliza con datos distintos, la API responde con **HTTP 409**.

## Tecnologías

**Android:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Room 3, SQLite, KSP, Retrofit, Gson, Coroutines, Flow, SharedPreferences y PdfRenderer.

**Backend:** Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, H2 y Maven Wrapper.

**Organización:** Git, GitHub y Trello.

## Rúbrica de evaluación

| Requisito | Implementación |
|---|---|
| Formularios | Login, registro, apuntes, entregas, comentarios |
| Validación | `Validaciones.kt` y mensajes visuales |
| Imagen | Logo de StudyDesk |
| Animación | Inicio y estados de carga |
| Modal | Confirmaciones, formularios y calendario |
| Persistencia | Room (usuarios, apuntes, entregas, comentarios) |
| Navegación | Navigation Compose y barra inferior |
| Recurso nativo | Notificaciones y compartir apuntes |
| Git y Trello | Control de versiones y organización del equipo |

## Pruebas realizadas

- Creación de apuntes y entregas con conexión.
- Creación local sin conexión y sincronización automática posterior.
- Repetición de creaciones sin duplicación por `idCliente`.
- Registros de igual contenido pero con identificadores distintos.
- Edición de apuntes en Room y Spring Boot con conexión.
- Eliminación local y remota de apuntes y entregas con conexión.
- Consultas API por correo e ID, y consultas SQL de H2.
- Pruebas de controladores e integración del backend.

## Limitaciones y posibles mejoras

StudyDesk es un **prototipo académico** y no un servicio listo para producción.

- El login es local y la API no incluye autenticación/autorización robustas de servidor.
- Las credenciales locales no tienen el tratamiento de seguridad necesario para producción.
- La sincronización automática actual recupera **creaciones pendientes**; **editar o eliminar sin conexión** no cuenta con una cola de cambios pendiente para aplicarse después.
- Los comentarios y los PDF no se sincronizan con el servidor.
- El backend se ejecuta localmente, no es un servidor publicado en Internet.
- La consola H2 solo debe habilitarse en el entorno local de demostración.
- Los avisos normativos son simulados y los PDF se actualizan manualmente.
- No existe un conjunto general de migraciones Room para futuros cambios estructurales.
- El material jurídico es académico; debe contrastarse con fuentes oficiales cuando importe su vigencia.

Mejoras futuras: autenticación segura, recuperación de cuentas, sincronización diferida de ediciones/eliminaciones y despliegue seguro para varios dispositivos.

---

**StudyDesk — Desarrollo de Aplicaciones Móviles, Duoc UC.**
