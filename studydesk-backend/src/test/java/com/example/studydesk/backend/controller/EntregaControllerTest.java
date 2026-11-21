
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Entrega;
import com.example.studydesk.backend.repository.EntregaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EntregaControllerTest {

    private final EntregaRepository repository =
            mock(EntregaRepository.class);

    private final EntregaController controller =
            new EntregaController(repository);

    // PRUEBA 1: Crear una entrega
    @Test
    void crearEntrega() {

        Entrega entrega = new Entrega();
        entrega.setTitulo("Informe final");
        entrega.setMateria("Programacion");
        entrega.setFecha("30 nov");
        entrega.setCorreoUsuario("paolo@gmail.com");

        when(repository.save(any(Entrega.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        Entrega resultado = controller.crear(entrega);

        assertEquals("Informe final", resultado.getTitulo());
        assertEquals("Programacion", resultado.getMateria());
        assertEquals("30 nov", resultado.getFecha());
        assertNull(resultado.getId());

        verify(repository).save(entrega);
    }

    // PRUEBA 2: Consultar una entrega existente
    @Test
    void obtenerEntregaPorId() {

        Entrega entrega = new Entrega();
        entrega.setId(1L);
        entrega.setTitulo("Prueba Kotlin");

        when(repository.findById(1L))
                .thenReturn(Optional.of(entrega));

        var respuesta = controller.obtenerPorId(1L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        assertEquals(
                "Prueba Kotlin",
                respuesta.getBody().getTitulo()
        );
    }

    // PRUEBA 3: Consultar una entrega inexistente
    @Test
    void obtenerEntregaInexistente() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        var respuesta = controller.obtenerPorId(999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );
    }

    // PRUEBA 4: Consultar entregas por correo
    @Test
    void obtenerEntregasPorCorreo() {

        Entrega entrega = new Entrega();
        entrega.setTitulo("Trabajo SQL");
        entrega.setCorreoUsuario("paolo@gmail.com");

        when(repository.findByCorreoUsuarioIgnoreCase(
                "paolo@gmail.com"
        )).thenReturn(List.of(entrega));

        var resultado = controller.obtenerPorCorreo(
                "PAOLO@GMAIL.COM"
        );

        assertEquals(1, resultado.size());

        assertEquals(
                "Trabajo SQL",
                resultado.get(0).getTitulo()
        );

        verify(repository)
                .findByCorreoUsuarioIgnoreCase(
                        "paolo@gmail.com"
                );
    }

    // PRUEBA 5: Editar una entrega existente
    @Test
    void editarEntrega() {

        Entrega original = new Entrega();
        original.setId(1L);
        original.setTitulo("Titulo original");
        original.setMateria("Programacion");
        original.setFecha("20 nov");

        Entrega nuevosDatos = new Entrega();
        nuevosDatos.setTitulo("Titulo actualizado");
        nuevosDatos.setMateria("Base de Datos");
        nuevosDatos.setFecha("30 nov");

        when(repository.findById(1L))
                .thenReturn(Optional.of(original));

        when(repository.save(any(Entrega.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        var respuesta = controller.editar(
                1L,
                nuevosDatos
        );

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());

        assertEquals(
                "Titulo actualizado",
                respuesta.getBody().getTitulo()
        );

        assertEquals(
                "Base de Datos",
                respuesta.getBody().getMateria()
        );

        assertEquals(
                "30 nov",
                respuesta.getBody().getFecha()
        );

        verify(repository).save(original);
    }

    // PRUEBA 6: Editar una entrega inexistente
    @Test
    void editarEntregaInexistente() {

        Entrega nuevosDatos = new Entrega();
        nuevosDatos.setTitulo("Nuevo titulo");

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        var respuesta = controller.editar(
                999L,
                nuevosDatos
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );

        verify(repository, never())
                .save(any(Entrega.class));
    }

    // PRUEBA 7: Eliminar una entrega existente
    @Test
    void eliminarEntrega() {

        when(repository.existsById(1L))
                .thenReturn(true);

        var respuesta = controller.eliminar(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                respuesta.getStatusCode()
        );

        verify(repository).deleteById(1L);
    }

    // PRUEBA 8: Eliminar una entrega inexistente
    @Test
    void eliminarEntregaInexistente() {

        when(repository.existsById(999L))
                .thenReturn(false);

        var respuesta = controller.eliminar(999L);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );

        verify(repository, never())
                .deleteById(999L);
    }
}
