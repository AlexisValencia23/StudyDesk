
package com.example.studydesk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studydesk.R
import com.example.studydesk.util.Validaciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Login(
    mensajeError: String? = null,
    onIniciarSesion: (correo: String, contrasena: String) -> Unit,
    onRegistro: () -> Unit = {}
) {

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Controla cuándo mostrar los errores visuales.
    var mostrarError by remember { mutableStateOf(false) }

    // Validaciones centralizadas.
    val correoError = if (mostrarError) {
        Validaciones.validarCorreo(correo)
    } else null

    val passwordError = if (mostrarError) {
        Validaciones.validarContrasena(password)
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {

        // Logo de StudyDesk.
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de StudyDesk",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de correo.
        Text(
            text = "Correo",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E2050)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { nuevoTexto ->
                correo = nuevoTexto
            },
            placeholder = { Text("nombre@correo.cl") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = correoError != null,
            supportingText = {
                if (correoError != null) {
                    Text(
                        text = correoError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña.
        Text(
            text = "Contraseña",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF1E2050)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { nuevoTexto ->
                password = nuevoTexto
            },
            placeholder = { Text("••••••••") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mensajes de error provenientes del ViewModel.
        if (mensajeError != null) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón de inicio de sesión.
        Button(
            onClick = {

                // Activar la validación visual de los campos.
                mostrarError = true

                val correoValido =
                    Validaciones.validarCorreo(correo) == null

                val passwordValida =
                    Validaciones.validarContrasena(password) == null

                // Solo iniciar sesión si ambos campos son válidos.
                if (correoValido && passwordValida) {
                    mostrarError = false
                    cargando = true

                    scope.launch {
                        // Conserva la animación de carga original.
                        delay(2000)

                        cargando = false

                        onIniciarSesion(
                            correo.trim(),
                            password
                        )
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3C4EB4)
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Iniciar sesión",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Aún no tienes cuenta?",
            color = Color(0xFF6E7191),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Navegación hacia Registro.
        OutlinedButton(
            onClick = onRegistro,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                Color(0xFF3C4EB4)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF3C4EB4)
            )
        ) {
            Text(
                text = "Registrarme",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
