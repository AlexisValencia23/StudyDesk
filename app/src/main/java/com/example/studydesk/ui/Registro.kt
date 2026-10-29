
package com.example.studydesk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.ui.theme.StudyDeskTheme
import com.example.studydesk.util.Validaciones

private val Indigo = Color(0xFF3949AB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)

@Composable
fun Registro(
    mensajeError: String? = null,
    mostrarCuentaCreada: Boolean = false,
    onAceptarCuentaCreada: () -> Unit = {},
    onCrearCuenta: (
        nombre: String,
        correo: String,
        contrasena: String,
        perfil: String
    ) -> Unit = { _, _, _, _ -> },
    onIniciarSesion: () -> Unit = {},
    onCambiarCorreo: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var perfil by remember {
        mutableStateOf("Estudiante de Derecho")
    }

    // Mostrar errores después del primer intento de registro
    var mostrarErrores by remember {
        mutableStateOf(false)
    }

    // Validaciones centralizadas
    val nombreError = if (mostrarErrores) {
        Validaciones.validarNombre(nombre)
    } else {
        null
    }

    val correoError = if (mostrarErrores) {
        Validaciones.validarCorreo(correo)
    } else {
        null
    }

    val contrasenaError = if (mostrarErrores) {
        Validaciones.validarContrasena(contrasena)
    } else {
        null
    }

    // Comprobar si el correo ya está registrado
    val correoDuplicado = if (
        mensajeError == "Ya existe una cuenta con ese correo"
    ) {
        mensajeError
    } else {
        null
    }

    // Mostrar error de formato o correo duplicado
    val errorCorreo = correoError ?: correoDuplicado

    val perfiles = listOf(
        "Estudiante de Derecho",
        "Abogado/a"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Center
    ) {

        // Encabezado
        Text(
            text = "StudyDesk",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Indigo
        )

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "Regístrate para estudiar y consultar legislación.",
            fontSize = 14.sp,
            color = TextoSuave,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            label = {
                Text("Nombre completo")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null
                )
            },
            isError = nombreError != null,
            supportingText = {
                if (nombreError != null) {
                    Text(
                        text = nombreError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo correo
        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it

                // Limpiar el error anterior al cambiar el correo
                onCambiarCorreo()
            },
            label = {
                Text("Correo")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null
                )
            },
            isError = errorCorreo != null,
            supportingText = {
                if (errorCorreo != null) {
                    Text(
                        text = errorCorreo,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
            },
            label = {
                Text("Contraseña")
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null
                )
            },
            isError = contrasenaError != null,
            supportingText = {
                Text(
                    text = contrasenaError ?: "Mínimo 8 caracteres",
                    color = if (contrasenaError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        TextoSuave
                    }
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Selección de perfil
        Text(
            text = "Soy",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuro
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            perfiles.forEach { opcion ->
                FilterChip(
                    selected = perfil == opcion,
                    onClick = {
                        perfil = opcion
                    },
                    label = {
                        Text(opcion)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mostrar otros errores generales
        // El correo duplicado ya se muestra bajo su campo
        if (
            mensajeError != null &&
            mensajeError != "Ya existe una cuenta con ese correo"
        ) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón crear cuenta
        Button(
            onClick = {
                mostrarErrores = true

                val nombreValido =
                    Validaciones.validarNombre(nombre) == null

                val correoValido =
                    Validaciones.validarCorreo(correo) == null

                val contrasenaValida =
                    Validaciones.validarContrasena(contrasena) == null

                if (
                    nombreValido &&
                    correoValido &&
                    contrasenaValida
                ) {
                    onCrearCuenta(
                        nombre,
                        correo,
                        contrasena,
                        perfil
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Indigo
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Crear cuenta",
                fontWeight = FontWeight.Bold
            )
        }

        // Volver al inicio de sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Ya tienes cuenta?",
                fontSize = 13.sp,
                color = TextoSuave
            )

            TextButton(
                onClick = onIniciarSesion
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Indigo,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Modal de cuenta creada
        if (mostrarCuentaCreada) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text("Cuenta creada")
                },
                text = {
                    Text(
                        "Tu cuenta se creó correctamente. " +
                                "¡Bienvenido/a a StudyDesk!"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onAceptarCuentaCreada,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Indigo
                        )
                    ) {
                        Text("Continuar")
                    }
                }
            )
        }
    }
}

// Vista previa
@Preview(showBackground = true)
@Composable
fun RegistroPreview() {
    StudyDeskTheme {
        Registro()
    }
}
