
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Apunte;
import com.example.studydesk.backend.repository.ApunteRepository;

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
@RequestMapping("/api/apuntes")
public class ApunteController {

    private final ApunteRepository apunteRepository;

    public ApunteController(ApunteRepository apunteRepository) {
        this.apunteRepository = apunteRepository;
    }

    // Consultar apuntes por ID local
    @GetMapping("/usuario/{usuarioId}")
    public List<Apunte> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return apunteRepository.findByUsuarioId(usuarioId);
    }

    // Consultar apuntes por correo
    @GetMapping("/usuario/correo/{correo}")
    public List<Apunte> obtenerPorCorreo(
            @PathVariable String correo) {

        String correoNormalizado = correo.trim()
                .toLowerCase(Locale.ROOT);

        if (correoNormalizado.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo no puede estar vacio"
            );
        }

        return apunteRepository
                .findByCorreoUsuarioIgnoreCase(correoNormalizado);
    }

    // Consultar un apunte por ID
    @GetMapping("/{id}")
    public ResponseEntity<Apunte> obtenerPorId(
            @PathVariable Long id) {

        return apunteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // CREAR APUNTE SIN GENERAR DUPLICADOS POR REINTENTO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Apunte crear(@RequestBody Apunte apunte) {

        apunte.setId(null);

        String idCliente = apunte.getIdCliente();

        // Compatibilidad con apuntes antiguos
        if (idCliente == null) {
            return apunteRepository.save(apunte);
        }

        if (idCliente.isBlank() || idCliente.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID de sincronizacion no valido"
            );
        }

        if (apunte.getCorreoUsuario() == null ||
                apunte.getCorreoUsuario().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Se necesita el correo del usuario"
            );
        }

        // Buscar un registro creado previamente
        Optional<Apunte> existente =
                apunteRepository.findByIdCliente(idCliente);

        if (existente.isPresent()) {
            return verificarReintento(
                    existente.get(),
                    apunte
            );
        }

        try {

            return apunteRepository.saveAndFlush(apunte);

        } catch (DataIntegrityViolationException e) {

            // Controlar peticiones simultaneas
            Optional<Apunte> creado =
                    apunteRepository.findByIdCliente(idCliente);

            if (creado.isPresent()) {
                return verificarReintento(
                        creado.get(),
                        apunte
                );
            }

            throw e;
        }
    }

    // Comprobar que el ID pertenece al mismo registro
    private Apunte verificarReintento(
            Apunte existente,
            Apunte recibido) {

        boolean coincide =
                Objects.equals(
                        existente.getCorreoUsuario(),
                        recibido.getCorreoUsuario()
                ) &&
                        Objects.equals(
                                existente.getMateria(),
                                recibido.getMateria()
                        ) &&
                        Objects.equals(
                                existente.getTitulo(),
                                recibido.getTitulo()
                        ) &&
                        Objects.equals(
                                existente.getContenido(),
                                recibido.getContenido()
                        ) &&
                        Objects.equals(
                                existente.getFecha(),
                                recibido.getFecha()
                        );

        if (!coincide) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ID de sincronizacion ya existe con otros datos"
            );
        }

        return existente;
    }

    // Editar un apunte existente
    @PutMapping("/{id}")
    public ResponseEntity<Apunte> editar(
            @PathVariable Long id,
            @RequestBody Apunte datos) {

        return apunteRepository.findById(id)
                .map(apunte -> {

                    apunte.setMateria(datos.getMateria());
                    apunte.setTitulo(datos.getTitulo());
                    apunte.setContenido(datos.getContenido());
                    apunte.setFecha(datos.getFecha());

                    return ResponseEntity.ok(
                            apunteRepository.save(apunte)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // Eliminar un apunte
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        if (!apunteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        apunteRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
