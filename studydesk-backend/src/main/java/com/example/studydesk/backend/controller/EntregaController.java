
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Entrega;
import com.example.studydesk.backend.repository.EntregaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    // Consultar entregas por ID local
    @GetMapping("/usuario/{usuarioId}")
    public List<Entrega> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return entregaRepository.findByUsuarioId(usuarioId);
    }

    // Consultar entregas por correo
    @GetMapping("/usuario/correo/{correo}")
    public List<Entrega> obtenerPorCorreo(
            @PathVariable String correo) {

        String correoNormalizado = correo.trim()
                .toLowerCase(Locale.ROOT);

        if (correoNormalizado.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo no puede estar vacio"
            );
        }

        return entregaRepository
                .findByCorreoUsuarioIgnoreCase(correoNormalizado);
    }

    // Consultar una entrega por ID
    @GetMapping("/{id}")
    public ResponseEntity<Entrega> obtenerPorId(
            @PathVariable Long id) {

        return entregaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // CREAR ENTREGA SIN GENERAR DUPLICADOS POR REINTENTO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Entrega crear(@RequestBody Entrega entrega) {

        // El ID remoto siempre lo genera Spring Boot
        entrega.setId(null);

        String idCliente = entrega.getIdCliente();

        // Compatibilidad con registros anteriores
        if (idCliente == null) {
            return entregaRepository.save(entrega);
        }

        if (idCliente.isBlank() || idCliente.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID de sincronizacion no valido"
            );
        }

        if (entrega.getCorreoUsuario() == null ||
                entrega.getCorreoUsuario().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Se necesita el correo del usuario"
            );
        }

        // Buscar si esta entrega ya fue recibida
        Optional<Entrega> existente =
                entregaRepository.findByIdCliente(idCliente);

        if (existente.isPresent()) {
            return verificarReintento(
                    existente.get(),
                    entrega
            );
        }

        try {

            // Crear y confirmar la escritura en H2
            return entregaRepository.saveAndFlush(entrega);

        } catch (DataIntegrityViolationException e) {

            // Otra peticion pudo crear el mismo ID
            Optional<Entrega> creada =
                    entregaRepository.findByIdCliente(idCliente);

            if (creada.isPresent()) {
                return verificarReintento(
                        creada.get(),
                        entrega
                );
            }

            // Si la causa fue distinta, conservar el error
            throw e;
        }
    }

    // Verificar que un ID no se reutilice con otros datos
    private Entrega verificarReintento(
            Entrega existente,
            Entrega recibida) {

        boolean coincide =
                Objects.equals(
                        existente.getCorreoUsuario(),
                        recibida.getCorreoUsuario()
                ) &&
                        Objects.equals(
                                existente.getMateria(),
                                recibida.getMateria()
                        ) &&
                        Objects.equals(
                                existente.getTitulo(),
                                recibida.getTitulo()
                        ) &&
                        Objects.equals(
                                existente.getFecha(),
                                recibida.getFecha()
                        );

        if (!coincide) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ID de sincronizacion ya existe con otros datos"
            );
        }

        // Devolver el registro original, sin crear otro
        return existente;
    }

    // Editar una entrega existente
    @PutMapping("/{id}")
    public ResponseEntity<Entrega> editar(
            @PathVariable Long id,
            @RequestBody Entrega datos) {

        return entregaRepository.findById(id)
                .map(entrega -> {

                    entrega.setMateria(datos.getMateria());
                    entrega.setTitulo(datos.getTitulo());
                    entrega.setFecha(datos.getFecha());

                    return ResponseEntity.ok(
                            entregaRepository.save(entrega)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Eliminar una entrega
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        if (!entregaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        entregaRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
