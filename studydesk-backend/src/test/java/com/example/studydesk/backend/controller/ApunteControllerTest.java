
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Apunte;
import com.example.studydesk.backend.repository.ApunteRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApunteControllerTest {

    private final ApunteRepository repository =
            mock(ApunteRepository.class);

    private final ApunteController controller =
            new ApunteController(repository);

    // PRUEBA 1: Crear un apunte
    @Test
    void crearApunte() {

        Apunte apunte = new Apunte();
        apunte.setTitulo("Apunte Java");
        apunte.setMateria("Programacion");
        apunte.setContenido("Contenido de prueba");
        apunte.setCorreoUsuario("paolo@gmail.com");

        when(repository.save(any(Apunte.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        Apunte resultado = controller.crear(apunte);

        assertEquals("Apunte Java", resultado.getTitulo());
        assertNull(resultado.getId());

        verify(repository).save(apunte);
    }

    // PRUEBA 2: Buscar un apunte existente
    @Test
    void obtenerApuntePorId() {

        Apunte apunte = new Apunte();
        apunte.setId(1L);
        apunte.setTitulo("Apunte Kotlin");

        when(repository.findById(1L))
                .thenReturn(Optional.of(apunte));

        var respuesta = controller.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(
                "Apunte Kotlin",
                respuesta.getBody().getTitulo()
        );
    }

    // PRUEBA 3: Buscar un apunte inexistente
    @Test
    void obtenerApunteInexistente() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        var respuesta = controller.obtenerPorId(999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );
    }

    // PRUEBA 4: Consultar apuntes por correo
    @Test
    void obtenerApuntesPorCorreo() {

        Apunte apunte = new Apunte();
        apunte.setTitulo("Apunte SQL");
        apunte.setCorreoUsuario("paolo@gmail.com");

        when(repository.findByCorreoUsuarioIgnoreCase(
                "paolo@gmail.com"
        )).thenReturn(List.of(apunte));

        var resultado = controller.obtenerPorCorreo(
                "PAOLO@GMAIL.COM"
        );

        assertEquals(1, resultado.size());
        assertEquals(
                "Apunte SQL",
                resultado.get(0).getTitulo()
        );
    }

    // PRUEBA 5: Editar un apunte
    @Test
    void editarApunte() {

        Apunte original = new Apunte();
        original.setId(1L);
        original.setTitulo("Titulo original");
        original.setContenido("Contenido original");

        Apunte nuevosDatos = new Apunte();
        nuevosDatos.setTitulo("Titulo editado");
        nuevosDatos.setContenido("Contenido actualizado");

        when(repository.findById(1L))
                .thenReturn(Optional.of(original));

        when(repository.save(any(Apunte.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        var respuesta = controller.editar(
                1L,
                nuevosDatos
        );

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());

        assertEquals(
                "Titulo editado",
                respuesta.getBody().getTitulo()
        );

        assertEquals(
                "Contenido actualizado",
                respuesta.getBody().getContenido()
        );

        verify(repository).save(original);
    }

    // PRUEBA 6: Eliminar un apunte existente
    @Test
    void eliminarApunte() {

        when(repository.existsById(1L))
                .thenReturn(true);

        var respuesta = controller.eliminar(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                respuesta.getStatusCode()
        );

        verify(repository).deleteById(1L);
    }

    // PRUEBA 7: Eliminar un apunte inexistente
    @Test
    void eliminarApunteInexistente() {

        when(repository.existsById(999L))
                .thenReturn(false);

        var respuesta = controller.eliminar(999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );

        verify(repository, never()).deleteById(999L);
    }
}
