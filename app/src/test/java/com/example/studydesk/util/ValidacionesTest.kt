package com.example.studydesk.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun `nombre vacio devuelve mensaje de obligatorio`() {
        assertEquals("El nombre es obligatorio", Validaciones.validarNombre("   "))
    }

    @Test
    fun `nombre de dos letras devuelve mensaje de largo minimo`() {
        assertEquals(
            "El nombre debe tener al menos 3 caracteres",
            Validaciones.validarNombre("Al")
        )
    }

    @Test
    fun `nombre valido no devuelve error`() {
        assertNull(Validaciones.validarNombre("Paolo"))
    }

    @Test
    fun `correo vacio devuelve mensaje de obligatorio`() {
        assertEquals("El correo es obligatorio", Validaciones.validarCorreo(""))
    }

    @Test
    fun `contrasena vacia devuelve mensaje de obligatoria`() {
        assertEquals("La contraseña es obligatoria", Validaciones.validarContrasena(""))
    }

    @Test
    fun `contrasena de siete caracteres devuelve mensaje de largo minimo`() {
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            Validaciones.validarContrasena("1234567")
        )
    }

    @Test
    fun `contrasena de ocho caracteres no devuelve error`() {
        assertNull(Validaciones.validarContrasena("12345678"))
    }

    @Test
    fun `materia de apunte vacia devuelve error`() {
        assertEquals("La materia es obligatoria", Validaciones.validarMateriaApunte(""))
    }

    @Test
    fun `titulo de apunte vacio devuelve error`() {
        assertEquals("El título es obligatorio", Validaciones.validarTituloApunte(""))
    }

    @Test
    fun `contenido de apunte vacio devuelve error`() {
        assertEquals("El contenido es obligatorio", Validaciones.validarContenidoApunte(""))
    }

    @Test
    fun `fecha de entrega vacia devuelve error`() {
        assertEquals(
            "Debes seleccionar una fecha de entrega",
            Validaciones.validarFechaEntrega("")
        )
    }

    @Test
    fun `comentario vacio devuelve error`() {
        assertEquals("Escribe algo antes de guardar", Validaciones.validarComentario("  "))
    }

    @Test
    fun `datos de entrega completos no devuelven error`() {
        assertNull(Validaciones.validarMateriaEntrega("Derecho Civil"))
        assertNull(Validaciones.validarTituloEntrega("Informe final"))
        assertNull(Validaciones.validarFechaEntrega("30 nov"))
    }
}