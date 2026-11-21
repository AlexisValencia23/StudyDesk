
package com.example.studydesk.backend.controller;

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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:studydesk_integracion_entregas;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class EntregaIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntregaRepository repository;

    // Limpiar solamente la base temporal de pruebas
    @BeforeEach
    void prepararPruebas() {
        repository.deleteAll();
    }

    // PRUEBA 1: Ciclo completo de una entrega
    @Test
    void crearConsultarEditarYEliminarEntrega() throws Exception {

        String nuevaEntrega = """
            {
                "materia": "Programacion",
                "titulo": "Trabajo de integracion",
                "fecha": "23 nov",
                "correoUsuario": "pruebas@example.com"
            }
            """;

        // PASO 1: Crear mediante POST
        MvcResult creacion = mockMvc.perform(
                        post("/api/entregas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(nuevaEntrega)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.titulo").value("Trabajo de integracion")
                )
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // Recuperar el ID generado por H2
        Number id = JsonPath.read(
                creacion.getResponse().getContentAsString(),
                "$.id"
        );

        // PASO 2: Consultar mediante GET
        mockMvc.perform(
                        get("/api/entregas/{id}", id.longValue())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.materia").value("Programacion")
                )
                .andExpect(
                        jsonPath("$.fecha").value("23 nov")
                );

        // PASO 3: Editar mediante PUT
        String entregaEditada = """
            {
                "materia": "Base de Datos",
                "titulo": "Trabajo actualizado",
                "fecha": "30 nov",
                "correoUsuario": "pruebas@example.com"
            }
            """;

        mockMvc.perform(
                        put("/api/entregas/{id}", id.longValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entregaEditada)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.titulo").value("Trabajo actualizado")
                )
                .andExpect(
                        jsonPath("$.materia").value("Base de Datos")
                );

        // PASO 4: Comprobar cambios en H2
        mockMvc.perform(
                        get("/api/entregas/{id}", id.longValue())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.fecha").value("30 nov")
                );

        // PASO 5: Eliminar mediante DELETE
        mockMvc.perform(
                        delete("/api/entregas/{id}", id.longValue())
                )
                .andExpect(status().isNoContent());

        // PASO 6: Confirmar que ya no existe
        mockMvc.perform(
                        get("/api/entregas/{id}", id.longValue())
                )
                .andExpect(status().isNotFound());

        assertFalse(repository.existsById(id.longValue()));
    }

    // PRUEBA 2: Filtrar las entregas por correo
    @Test
    void consultarEntregasFiltradasPorCorreo() throws Exception {

        String entregaUno = """
            {
                "materia": "Java",
                "titulo": "Entrega usuario uno",
                "fecha": "24 nov",
                "correoUsuario": "uno@example.com"
            }
            """;

        String entregaDos = """
            {
                "materia": "Kotlin",
                "titulo": "Entrega usuario dos",
                "fecha": "25 nov",
                "correoUsuario": "dos@example.com"
            }
            """;

        // Crear la primera entrega
        mockMvc.perform(
                        post("/api/entregas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entregaUno)
                )
                .andExpect(status().isCreated());

        // Crear la segunda entrega
        mockMvc.perform(
                        post("/api/entregas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(entregaDos)
                )
                .andExpect(status().isCreated());

        // Comprobar que ambas existen en H2
        assertEquals(2, repository.count());

        // Consultar solamente las entregas del primer correo
        mockMvc.perform(
                        get(
                                "/api/entregas/usuario/correo/{correo}",
                                "uno@example.com"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath("$[0].titulo").value("Entrega usuario uno")
                )
                .andExpect(
                        jsonPath("$[0].correoUsuario").value("uno@example.com")
                );
    }
}
