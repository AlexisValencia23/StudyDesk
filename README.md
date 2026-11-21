# StudyDesk

**Aplicación Android para organizar actividades académicas, gestionar apuntes y consultar material de estudio jurídico.**

Proyecto desarrollado para **Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC — caso 13**. Integra una aplicación nativa en **Kotlin y Jetpack Compose**, persistencia local con **Room** y una API REST construida con **Spring Boot y H2**.

## Inicio rápido

Estos comandos permiten iniciar la aplicación, consultar las tablas del servidor y ejecutar las pruebas del proyecto.

### 1. Iniciar el backend

**Requisitos:** JDK 21, Windows y PowerShell. Abrir una terminal en la **carpeta principal del repositorio** y ejecutar:

```powershell
cd .\studydesk-backend
$env:SPRING_H2_CONSOLE_ENABLED = "true"
.\mvnw.cmd spring-boot:run
```

La variable habilita la consola SQL de H2 únicamente para esta ejecución local. **Mantener abierta esta terminal** mientras se utiliza el backend. El servidor se inicia en `http://localhost:8080`.

Si la terminal ya está dentro de `studydesk-backend`, ejecutar solo las dos últimas líneas. Para comprobar que el puerto está disponible, utilizar **otra terminal**:

```powershell
Test-NetConnection localhost -Port 8080
```

El campo `TcpTestSucceeded` debe indicar `True`.

> **Importante:** iniciar el backend desde `studydesk-backend`. La ruta del archivo de base de datos es relativa al directorio de ejecución; iniciarlo desde otra ubicación podría abrir otra base H2.

### 2. Abrir la consola SQL de H2

Con el backend iniciado, abrir en un navegador:

**[http://localhost:8080/h2-console](http://localhost:8080/h2-console)**

Introducir estos valores:

| Campo | Valor |
|---|---|
| **Driver Class** | `org.h2.Driver` |
| **JDBC URL** | `jdbc:h2:file:./data/studydesk` |
| **User Name** | `sa` |
| **Password** | Vacío |

Seleccionar **Connect**. Desde el editor SQL se pueden consultar las tablas de H2 con los siguientes comandos:

```sql
-- Listar las tablas disponibles
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'PUBLIC'
ORDER BY TABLE_NAME;

-- Consultar entregas, comenzando por las más recientes
SELECT ID, TITULO, MATERIA, FECHA, CORREO_USUARIO, ID_CLIENTE
FROM ENTREGAS
ORDER BY ID DESC;

-- Consultar apuntes, comenzando por los más recientes
SELECT ID, TITULO, MATERIA, CONTENIDO, FECHA, CORREO_USUARIO, ID_CLIENTE
FROM APUNTES
ORDER BY ID DESC;

-- Contar los registros
SELECT COUNT(*) AS TOTAL_ENTREGAS FROM ENTREGAS;
SELECT COUNT(*) AS TOTAL_APUNTES FROM APUNTES;
```

Para filtrar los registros por correo, sustituir el valor del ejemplo por el correo de una cuenta existente:

```sql
SELECT ID, TITULO, MATERIA, FECHA, ID_CLIENTE
FROM ENTREGAS
WHERE LOWER(CORREO_USUARIO) = LOWER('alumno@example.com')
ORDER BY ID DESC;
```

Las consultas `SELECT` son suficientes para examinar los registros **sin alterar los datos**. Para comprobar una inserción, crear una nueva entrega o un apunte desde Android y volver a ejecutar la consulta correspondiente en H2.

**¿Por qué H2 Console y no MySQL Workbench?** El backend usa una base **H2**, no MySQL. MySQL Workbench y HeidiSQL no se conectan directamente a esa base tal como está configurada. La consola H2 incluida en el proyecto permite ejecutar SQL y visualizar los datos sin migraciones ni instalaciones adicionales.

### 3. Ejecutar Android

1. Abrir la carpeta principal de **StudyDesk** en Android Studio.
2. Esperar la sincronización de Gradle.
3. Elegir un emulador Android y presionar **Run**.
4. Iniciar sesión o registrar una cuenta.
5. Crear apuntes y entregas y consultar sus registros desde la pestaña **Servidor** o la consola H2.

**Direcciones del backend:**

| Entorno | URL |
|---|---|
| Navegador o PowerShell en Windows | `http://localhost:8080/` |
| Emulador de Android Studio | `http://10.0.2.2:8080/` |

`10.0.2.2` permite que el emulador acceda al equipo anfitrión. Un teléfono físico necesitaría una configuración de red diferente.

### 4. Consultar los datos desde PowerShell

Además de SQL, el backend ofrece una API REST. En otra terminal, escribir el correo de la cuenta cuyos registros se quieren consultar:

```powershell
$correo = "alumno@example.com"
$correoUrl = [uri]::EscapeDataString($correo)

Invoke-RestMethod "http://localhost:8080/api/entregas/usuario/correo/$correoUrl" | Format-Table id, titulo, materia, fecha, idCliente
Invoke-RestMethod "http://localhost:8080/api/apuntes/usuario/correo/$correoUrl" | Format-Table id, titulo, materia, fecha, idCliente
```

Consultar un registro por su ID remoto:

```powershell
Invoke-RestMethod "http://localhost:8080/api/entregas/1" | Format-List
Invoke-RestMethod "http://localhost:8080/api/apuntes/1" | Format-List
```

Los IDs `1` son ejemplos: reemplazarlos por los valores que existan en H2. Un registro inexistente devuelve **HTTP 404**.

### 5. Ejecutar las pruebas del backend

Desde una terminal ubicada en `studydesk-backend`:

```powershell
.\mvnw.cmd clean test
```

El resultado esperado es `BUILD SUCCESS`. El proyecto incluye pruebas de controladores, integración de la API y prevención de duplicados. Las pruebas de integración utilizan bases de datos **H2 temporales en memoria**, separadas de la base persistente del proyecto.

### 6. Verificar la sincronización sin conexión

1. Iniciar sesión en StudyDesk con el backend funcionando.
2. Detener Spring Boot con **Ctrl+C** en la terminal donde se ejecuta.
3. Crear un apunte y una entrega. Ambos deben quedar guardados en **Room**, incluso cuando el servidor no responde.
4. Iniciar nuevamente Spring Boot con los comandos del paso 1.
5. Regresar a StudyDesk o volver a abrir la aplicación: se comprobarán los registros pendientes.
6. Consultar la pestaña **Servidor**, la consola H2 o los endpoints REST para confirmar que aparecen.
7. Repetir la sincronización y comprobar que no se generan registros duplicados.

En Android Studio, filtrar **Logcat** por la etiqueta `StudyDeskSync`. Los errores de conexión mientras Spring Boot está detenido son esperados. Una sincronización recuperada puede mostrar mensajes similares a los siguientes:

```text
StudyDeskSync: Pendiente vinculado. Room: 5, Spring Boot: 11
StudyDeskSync: Entrega pendiente vinculada. Room: 7, Spring Boot: 46
```

Los IDs varían entre instalaciones.

---

## Características principales

### Autenticación y cuentas

- Registro e inicio de sesión con nombre, correo, contraseña y perfil.
- Validaciones de formularios y comprobación de correos duplicados.
- Persistencia de usuarios con Room y conservación de la sesión con SharedPreferences.
- Separación de apuntes y entregas según el usuario que inició sesión.

### Inicio y organización

- Pantalla de inicio con bienvenida personalizada, entregas próximas y apuntes recientes.
- Animaciones, navegación mediante barra inferior y notificación demostrativa de actualización normativa.
- Gestión de entregas: creación, listado, selección de fecha, filtros y eliminación con confirmación.
- Gestión de apuntes: creación, lectura, edición, eliminación, filtros y opción para compartir mediante aplicaciones de Android.

### Material jurídico y comentarios

- Consulta local de documentos PDF jurídicos, como Código Civil, Código Penal y material de Derecho Constitucional.
- Búsqueda de documentos y lector integrado mediante `PdfRenderer`.
- Comentarios asociados a documentos, con creación, edición y eliminación de comentarios propios.
- Los PDF se almacenan en `app/src/main/assets/`; su información está definida en `model/MaterialEstudio.kt`.

### Integración con Spring Boot

- API REST para apuntes y entregas.
- Guardado local en Room y sincronización de creaciones con el backend.
- Sincronización automática de pendientes al regresar a la aplicación.
- Pantalla **Servidor** para consultar registros remotos y ejecutar verificaciones manuales.
- Identificador estable `idCliente` para reconocer reintentos y evitar duplicados de creación.

## Arquitectura

StudyDesk utiliza una organización **MVVM (Model–View–ViewModel)** y una capa de repositorios.

| Componente | Responsabilidad |
|---|---|
| `app/src/main/java/com/example/studydesk/ui/` | Pantallas desarrolladas con Jetpack Compose |
| `viewmodel/` | Estado y lógica de las pantallas |
| `repository/` | Operaciones de Room y servicios de la API |
| `model/` | Entidades, DAO y base de datos Room |
| `network/` | Retrofit y modelos de transferencia de datos |
| `util/` | Validaciones, fechas, sesión e identificadores de sincronización |
| `notification/` | Notificaciones nativas |
| `pdf/` | Lectura de documentos PDF |
| `MainActivity.kt` | Inicialización de la aplicación y sincronización al regresar |
| `studydesk-backend/` | Servicio Java con Spring Boot, JPA y H2 |

### Persistencia de datos

| Información | Almacenamiento |
|---|---|
| Usuarios | Room (Android) |
| Sesión activa | SharedPreferences (Android) |
| Apuntes | Room y H2 |
| Entregas | Room y H2 |
| Comentarios | Room (Android) |
| Material jurídico | Archivos PDF locales |

Las bases **Room y H2 son independientes**. H2 no guarda las cuentas locales ni los comentarios. El backend conserva la información sincronizada de apuntes y entregas.

La configuración H2 se encuentra en `studydesk-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/studydesk
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

La base persistente se crea en la carpeta `studydesk-backend/data/`, excluida del repositorio mediante `.gitignore`. **Al clonar el proyecto en otro equipo no se transfieren automáticamente los datos creados en una instalación anterior.**

## Tecnologías utilizadas

- **Aplicación móvil:** Kotlin, Android Studio, Jetpack Compose, Material 3, Navigation Compose, Room 3, SQLite, KSP, Coroutines y Flow.
- **Integración:** Retrofit 3 y Gson.
- **Backend:** Java 21, Spring Boot 4.1.2, Spring Data JPA, H2 y Maven Wrapper.
- **Recursos Android:** SharedPreferences, notificaciones, PdfRenderer y función nativa para compartir contenido.
- **Colaboración:** Git, GitHub y Trello.

## Requisitos de ejecución

- **JDK 21** para Spring Boot.
- Android Studio y SDK de Android configurados.
- Emulador o dispositivo con **Android 7.0 (API 24)** o superior.
- Conexión a Internet durante la descarga inicial de dependencias.
- **Maven no necesita instalarse por separado**: el backend incluye Maven Wrapper (`mvnw.cmd`).

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/apuntes/usuario/correo/{correo}` | Listar apuntes de un correo |
| `GET` | `/api/apuntes/{id}` | Consultar un apunte |
| `POST` | `/api/apuntes` | Crear un apunte |
| `PUT` | `/api/apuntes/{id}` | Editar un apunte |
| `DELETE` | `/api/apuntes/{id}` | Eliminar un apunte |
| `GET` | `/api/entregas/usuario/correo/{correo}` | Listar entregas de un correo |
| `GET` | `/api/entregas/{id}` | Consultar una entrega |
| `POST` | `/api/entregas` | Crear una entrega |
| `PUT` | `/api/entregas/{id}` | Editar una entrega |
| `DELETE` | `/api/entregas/{id}` | Eliminar una entrega |

El backend asocia los registros a su correo y reconoce el campo `idCliente` en las solicitudes de creación. La repetición de un mismo `idCliente` con datos diferentes devuelve `HTTP 409`.

## Requisitos académicos implementados

| Requisito | Implementación |
|---|---|
| Formularios | Login, registro, entregas, apuntes y comentarios |
| Validación de campos | Clase `Validaciones.kt` y mensajes en interfaz |
| Imagen | Logo de StudyDesk |
| Animación | Animaciones en Inicio y estados de carga |
| Modal | Formularios, calendarios y confirmaciones |
| Persistencia local | Room para usuarios, apuntes, entregas y comentarios |
| Navegación | Navigation Compose y barra inferior |
| Recurso nativo | Notificaciones y función Compartir |
| Colaboración | Git/GitHub y Trello |

## Validaciones realizadas

Se realizaron comprobaciones del funcionamiento de la aplicación y de la API, entre ellas:

- Creación de apuntes y entregas con conexión.
- Guardado local sin conexión y sincronización de registros pendientes después de recuperar el servidor.
- Reintentos de creación sin duplicados utilizando `idCliente`.
- Creación de registros independientes con el mismo contenido.
- Edición de apuntes y entregas con conexión.
- Eliminación de registros locales y remotos con conexión.
- Consultas REST por correo e ID, y consultas SQL a H2.
- Pruebas automatizadas de controladores e integración.

## Limitaciones y mejoras futuras

StudyDesk es un **prototipo académico**. Entre las limitaciones de esta versión se encuentran:

- La autenticación de usuarios es local y los endpoints del backend no incluyen autorización adecuada para un servicio público.
- El almacenamiento de contraseñas no está preparado para producción.
- La sincronización automática se centra en **creaciones pendientes**: las ediciones y eliminaciones efectuadas sin conexión no disponen de una cola de operaciones diferidas.
- Los comentarios y archivos PDF no se sincronizan con Spring Boot.
- Los avisos de actualización normativa son simulados y las fechas de los materiales se mantienen manualmente.
- La consola H2 debe habilitarse **solo en entornos locales de desarrollo**.
- Las migraciones de esquema de Room para futuras versiones no están contempladas en este prototipo.

Entre las mejoras posteriores posibles están el uso de autenticación segura, el despliegue de un backend accesible entre dispositivos, la sincronización de todas las operaciones y la actualización automática de material jurídico.

## Equipo

- Paolo Muñoz
- Benjamín Farias
- Alexis Valencia

---

**StudyDesk · Duoc UC · Desarrollo de Aplicaciones Móviles**
