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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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

private val Indigo = Color(0xFF3949AB)
private val TextoOscuro = Color(0xFF1B1F3B)
private val TextoSuave = Color(0xFF4A5073)

@Composable
fun Registro(
    onCrearCuenta: (nombre: String, correo: String, contrasena: String, perfil: String) -> Unit = { _, _, _, _ -> },
    onIniciarSesion: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf("Estudiante de Derecho") }

    val perfiles = listOf("Estudiante de Derecho", "Abogado/a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "StudyFlow",
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

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            supportingText = { Text("Mínimo 8 caracteres") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Soy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)

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
                    onClick = { perfil = opcion },
                    label = { Text(opcion) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onCrearCuenta(nombre, correo, contrasena, perfil) },
            enabled = nombre.isNotBlank() && correo.isNotBlank() && contrasena.length >= 8,
            colors = ButtonDefaults.buttonColors(containerColor = Indigo),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Crear cuenta", fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿Ya tienes cuenta?", fontSize = 13.sp, color = TextoSuave)
            TextButton(onClick = onIniciarSesion) {
                Text(text = "Iniciar sesión", color = Indigo, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroPreview() {
    StudyDeskTheme {
        Registro()
    }
}