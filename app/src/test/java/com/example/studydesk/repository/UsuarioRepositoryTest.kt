package com.example.studydesk.repository

import com.example.studydesk.model.Usuario
import com.example.studydesk.model.UsuarioDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuarioRepositoryTest {

    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val repository = UsuarioRepository(usuarioDao)

    private val usuarioEjemplo = Usuario(
        id = 1,
        nombre = "Paolo",
        correo = "paolo@correo.cl",
        contrasena = "12345678",
        perfil = "Estudiante de Derecho"
    )

    @Test
    fun `crearUsuario inserta el usuario en el dao`() = runTest {
        repository.crearUsuario(usuarioEjemplo)

        coVerify(exactly = 1) { usuarioDao.insertar(usuarioEjemplo) }
    }

    @Test
    fun `buscarPorCorreo devuelve el usuario que entrega el dao`() = runTest {
        coEvery { usuarioDao.buscarPorCorreo("paolo@correo.cl") } returns usuarioEjemplo

        assertEquals(usuarioEjemplo, repository.buscarPorCorreo("paolo@correo.cl"))
    }

    @Test
    fun `buscarPorCorreo devuelve null cuando el correo no existe`() = runTest {
        coEvery { usuarioDao.buscarPorCorreo("nadie@correo.cl") } returns null

        assertNull(repository.buscarPorCorreo("nadie@correo.cl"))
    }
}