package com.example.studydesk.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Campo de texto con ícono a la izquierda y retroalimentación visual de error.
// No valida nada: solo muestra el mensaje de error que le entrega el ViewModel.
@Composable
fun CampoFormulario(
    valor: String,
    onValorCambiado: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esContrasena: Boolean = false,
    ayuda: String? = null
) {
    var contrasenaVisible by remember { mutableStateOf(false) }
    val tieneError = error != null

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (tieneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (esContrasena) {
                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                    Icon(
                        imageVector = if (contrasenaVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            } else if (tieneError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) {
                Text(text = texto)
            }
        },
        isError = tieneError,
        singleLine = true,
        visualTransformation = if (esContrasena && !contrasenaVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}