package com.example.studydesk.viewmodel

import com.example.studydesk.model.Usuario
import com.example.studydesk.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelTest {

    private val repository = mockk<UsuarioRepository>(relaxed = true)
    private lateinit var viewModel: UsuarioViewModel

    private val usuarioGuardado = Usuario(
        id = 1,
        nombre = "Paolo",
        correo = "paolo@correo.cl",
        contrasena = "12345678",
        perfil = "Estudiante de Derecho"
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = UsuarioViewModel(repository)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `registrar con nombre corto muestra error y no crea la cuenta`() {
        viewModel.registrar("Al", "paolo@correo.cl", "12345678", "Estudiante de Derecho") {}

        assertEquals("El nombre debe tener al menos 3 caracteres", viewModel.mensajeError)
        coVerify(exactly = 0) { repository.crearUsuario(any()) }
    }

    @Test
    fun `registrar con correo ya existente muestra error`() {
        coEvery { repository.buscarPorCorreo("paolo@correo.cl") } returns usuarioGuardado

        viewModel.registrar("Paolo", "paolo@correo.cl", "12345678", "Estudiante de Derecho") {}

        assertEquals("Ya existe una cuenta con ese correo", viewModel.mensajeError)
        coVerify(exactly = 0) { repository.crearUsuario(any()) }
    }

    @Test
    fun `registrar con datos validos crea la cuenta y avisa el exito`() {
        coEvery { repository.buscarPorCorreo("paolo@correo.cl") } returnsMany
                listOf(null, usuarioGuardado)
        var usuarioRecibido: Usuario? = null

        viewModel.registrar(" Paolo ", " Paolo@Correo.cl ", "12345678", "Estudiante de Derecho") {
            usuarioRecibido = it
        }

        coVerify(exactly = 1) {
            repository.crearUsuario(match { it.nombre == "Paolo" && it.correo == "paolo@correo.cl" })
        }
        assertEquals(usuarioGuardado, usuarioRecibido)
        assertNull(viewModel.mensajeError)
    }

    @Test
    fun `iniciarSesion con correo invalido muestra error`() {
        viewModel.iniciarSesion("paolo.correo.cl", "12345678") {}

        assertEquals("Ingresa un correo válido", viewModel.mensajeError)
    }

    @Test
    fun `iniciarSesion con datos correctos entrega el usuario`() {
        coEvery { repository.buscarPorCorreo("paolo@correo.cl") } returns usuarioGuardado
        var usuarioRecibido: Usuario? = null

        viewModel.iniciarSesion("paolo@correo.cl", "12345678") { usuarioRecibido = it }

        assertEquals(usuarioGuardado, usuarioRecibido)
        assertNull(viewModel.mensajeError)
    }

    @Test
    fun `iniciarSesion con contrasena incorrecta muestra error`() {
        coEvery { repository.buscarPorCorreo("paolo@correo.cl") } returns usuarioGuardado
        var usuarioRecibido: Usuario? = null

        viewModel.iniciarSesion("paolo@correo.cl", "contrasena-mala") { usuarioRecibido = it }

        assertNull(usuarioRecibido)
        assertEquals("Correo o contraseña incorrectos", viewModel.mensajeError)
    }

    @Test
    fun `limpiarError borra el mensaje de error`() {
        viewModel.iniciarSesion("paolo.correo.cl", "12345678") {}

        viewModel.limpiarError()

        assertNull(viewModel.mensajeError)
    }
}