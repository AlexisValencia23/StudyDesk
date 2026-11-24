package com.example.studydesk.viewmodel

import com.example.studydesk.model.Comentario
import com.example.studydesk.repository.ComentarioRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComentarioViewModelTest {

    private val repository = mockk<ComentarioRepository>(relaxed = true)
    private lateinit var viewModel: ComentarioViewModel

    private val comentarioPropio = Comentario(
        id = 10,
        archivoPdf = "codigo_civil.pdf",
        autor = "Paolo",
        autorId = 1,
        texto = "Buen material",
        fecha = "20 nov"
    )
    private val comentarioAjeno = comentarioPropio.copy(
        id = 11,
        autor = "Benjamin",
        autorId = 2
    )

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ComentarioViewModel(repository)
        viewModel.establecerUsuario(1)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `agregarComentario guarda el texto sin espacios y con el id del usuario activo`() {
        viewModel.agregarComentario("codigo_civil.pdf", "Paolo", "  Hola  ")

        val capturado = slot<Comentario>()
        coVerify(exactly = 1) { repository.agregarComentario(capture(capturado)) }
        assertEquals("Hola", capturado.captured.texto)
        assertEquals(1, capturado.captured.autorId)
    }

    @Test
    fun `agregarComentario con texto vacio no guarda nada`() {
        viewModel.agregarComentario("codigo_civil.pdf", "Paolo", "   ")

        coVerify(exactly = 0) { repository.agregarComentario(any()) }
    }

    @Test
    fun `agregarComentario sin usuario activo no guarda nada`() {
        viewModel.establecerUsuario(null)

        viewModel.agregarComentario("codigo_civil.pdf", "Paolo", "Hola")

        coVerify(exactly = 0) { repository.agregarComentario(any()) }
    }

    @Test
    fun `editarComentario propio actualiza el comentario`() {
        viewModel.editarComentario(comentarioPropio, "Texto nuevo")

        coVerify(exactly = 1) { repository.editarComentario(any(), 1) }
    }

    @Test
    fun `editarComentario ajeno no actualiza nada`() {
        viewModel.editarComentario(comentarioAjeno, "Texto nuevo")

        coVerify(exactly = 0) { repository.editarComentario(any(), any()) }
    }

    @Test
    fun `eliminarComentario propio lo elimina`() {
        viewModel.eliminarComentario(comentarioPropio)

        coVerify(exactly = 1) { repository.eliminarComentario(10, 1) }
    }

    @Test
    fun `eliminarComentario ajeno no elimina nada`() {
        viewModel.eliminarComentario(comentarioAjeno)

        coVerify(exactly = 0) { repository.eliminarComentario(any(), any()) }
    }
}