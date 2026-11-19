
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Apunte;
import com.example.studydesk.backend.repository.ApunteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/apuntes")
public class ApunteController {

    private final ApunteRepository apunteRepository;

    public ApunteController(ApunteRepository apunteRepository) {
        this.apunteRepository = apunteRepository;
    }

    // GET: Obtener apuntes por ID de usuario
    // Se conserva para compatibilidad
    @GetMapping("/usuario/{usuarioId}")
    public List<Apunte> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return apunteRepository.findByUsuarioId(usuarioId);
    }

    // GET: Obtener apuntes por correo electronico
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

    // GET: Obtener un apunte por ID
    @GetMapping("/{id}")
    public ResponseEntity<Apunte> obtenerPorId(
            @PathVariable Long id) {

        return apunteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Crear un apunte
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Apunte crear(@RequestBody Apunte apunte) {

        apunte.setId(null);

        return apunteRepository.save(apunte);
    }

    // PUT: Editar un apunte
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

    // DELETE: Eliminar un apunte
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
