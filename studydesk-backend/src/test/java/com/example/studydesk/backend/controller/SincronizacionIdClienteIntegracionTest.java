
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.repository.ApunteRepository;
import com.example.studydesk.backend.repository.EntregaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:studydesk_test_duplicados;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class SincronizacionIdClienteIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApunteRepository apunteRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @BeforeEach
    void preparar() {
        apunteRepository.deleteAll();
        entregaRepository.deleteAll();
    }

    private long crear(String ruta, String json)
            throws Exception {

        MvcResult resultado = mockMvc.perform(
                        post(ruta)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(
                resultado.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    @Test
    void entregaRepetidaNoGeneraDuplicados()
            throws Exception {

        String json = """
            {
              "materia": "Programacion",
              "titulo": "Trabajo final",
              "fecha": "30 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "entrega-001"
            }
            """;

        long primero = crear("/api/entregas", json);
        long segundo = crear("/api/entregas", json);

        assertEquals(primero, segundo);
        assertEquals(1, entregaRepository.count());
    }

    @Test
    void apunteRepetidoNoGeneraDuplicados()
            throws Exception {

        String json = """
            {
              "materia": "Java",
              "titulo": "Resumen",
              "contenido": "Contenido de prueba",
              "fecha": "30 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "apunte-001"
            }
            """;

        long primero = crear("/api/apuntes", json);
        long segundo = crear("/api/apuntes", json);

        assertEquals(primero, segundo);
        assertEquals(1, apunteRepository.count());
    }

    @Test
    void entregasIgualesConIdsDistintosSeConservan()
            throws Exception {

        String primera = """
            {
              "materia": "SQL",
              "titulo": "Informe",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "entrega-A"
            }
            """;

        String segunda = """
            {
              "materia": "SQL",
              "titulo": "Informe",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "entrega-B"
            }
            """;

        long idPrimero = crear("/api/entregas", primera);
        long idSegundo = crear("/api/entregas", segunda);

        assertNotEquals(idPrimero, idSegundo);
        assertEquals(2, entregaRepository.count());
    }

    @Test
    void apuntesIgualesConIdsDistintosSeConservan()
            throws Exception {

        String primero = """
            {
              "materia": "Kotlin",
              "titulo": "Apunte",
              "contenido": "Texto",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "apunte-A"
            }
            """;

        String segundo = """
            {
              "materia": "Kotlin",
              "titulo": "Apunte",
              "contenido": "Texto",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "apunte-B"
            }
            """;

        long idPrimero = crear("/api/apuntes", primero);
        long idSegundo = crear("/api/apuntes", segundo);

        assertNotEquals(idPrimero, idSegundo);
        assertEquals(2, apunteRepository.count());
    }

    @Test
    void entregaConIdReutilizadoYDatosDistintosDaConflicto()
            throws Exception {

        String original = """
            {
              "materia": "Java",
              "titulo": "Original",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "entrega-conflicto"
            }
            """;

        String diferente = """
            {
              "materia": "Java",
              "titulo": "Modificada",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "entrega-conflicto"
            }
            """;

        crear("/api/entregas", original);

        mockMvc.perform(
                        post("/api/entregas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(diferente)
                )
                .andExpect(status().isConflict());

        assertEquals(1, entregaRepository.count());
    }

    @Test
    void apunteConIdReutilizadoYDatosDistintosDaConflicto()
            throws Exception {

        String original = """
            {
              "materia": "Java",
              "titulo": "Apunte original",
              "contenido": "Texto original",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "apunte-conflicto"
            }
            """;

        String diferente = """
            {
              "materia": "Java",
              "titulo": "Apunte original",
              "contenido": "Texto modificado",
              "fecha": "25 nov",
              "correoUsuario": "test@example.com",
              "idCliente": "apunte-conflicto"
            }
            """;

        crear("/api/apuntes", original);

        mockMvc.perform(
                        post("/api/apuntes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(diferente)
                )
                .andExpect(status().isConflict());

        assertEquals(1, apunteRepository.count());
    }
}
