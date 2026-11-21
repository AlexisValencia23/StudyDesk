
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.repository.ApunteRepository;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:studydesk_integracion_apuntes;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ApunteIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApunteRepository repository;

    // Limpiar solamente la base temporal de pruebas
    @BeforeEach
    void prepararPruebas() {
        repository.deleteAll();
    }

    // PRUEBA 1: Ciclo completo de un apunte
    @Test
    void crearConsultarEditarYEliminarApunte() throws Exception {

        String nuevoApunte = """
            {
                "materia": "Programacion",
                "titulo": "Prueba integracion",
                "contenido": "Contenido original",
                "fecha": "20 nov",
                "correoUsuario": "pruebas@example.com"
            }
            """;

        // PASO 1: Crear mediante POST
        MvcResult creacion = mockMvc.perform(
                        post("/api/apuntes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(nuevoApunte)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.titulo").value("Prueba integracion")
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
                        get("/api/apuntes/{id}", id.longValue())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.contenido").value("Contenido original")
                );

        // PASO 3: Editar mediante PUT
        String apunteEditado = """
            {
                "materia": "Programacion",
                "titulo": "Apunte actualizado",
                "contenido": "Contenido modificado",
                "fecha": "21 nov",
                "correoUsuario": "pruebas@example.com"
            }
            """;

        mockMvc.perform(
                        put("/api/apuntes/{id}", id.longValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(apunteEditado)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.titulo").value("Apunte actualizado")
                );

        // PASO 4: Comprobar el cambio en la base
        mockMvc.perform(
                        get("/api/apuntes/{id}", id.longValue())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.contenido").value("Contenido modificado")
                );

        // PASO 5: Eliminar mediante DELETE
        mockMvc.perform(
                        delete("/api/apuntes/{id}", id.longValue())
                )
                .andExpect(status().isNoContent());

        // PASO 6: Confirmar que ya no existe
        mockMvc.perform(
                        get("/api/apuntes/{id}", id.longValue())
                )
                .andExpect(status().isNotFound());
    }

    // PRUEBA 2: Consultar solamente apuntes de un correo
    @Test
    void consultarApuntesFiltradosPorCorreo() throws Exception {

        String apunteUno = """
            {
                "materia": "Java",
                "titulo": "Apunte usuario uno",
                "contenido": "Contenido uno",
                "fecha": "20 nov",
                "correoUsuario": "uno@example.com"
            }
            """;

        String apunteDos = """
            {
                "materia": "Kotlin",
                "titulo": "Apunte usuario dos",
                "contenido": "Contenido dos",
                "fecha": "20 nov",
                "correoUsuario": "dos@example.com"
            }
            """;

        // Guardar dos apuntes de diferentes usuarios
        mockMvc.perform(
                        post("/api/apuntes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(apunteUno)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/apuntes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(apunteDos)
                )
                .andExpect(status().isCreated());

        // Consultar solamente los apuntes del primer correo
        mockMvc.perform(
                        get("/api/apuntes/usuario/correo/{correo}",
                                "uno@example.com")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(
                        jsonPath("$[0].titulo").value("Apunte usuario uno")
                )
                .andExpect(
                        jsonPath("$[0].correoUsuario").value("uno@example.com")
                );
    }
}
