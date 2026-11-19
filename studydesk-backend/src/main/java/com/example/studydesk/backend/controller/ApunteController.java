
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Apunte;
import com.example.studydesk.backend.repository.ApunteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apuntes")
public class ApunteController {

    private final ApunteRepository apunteRepository;

    public ApunteController(ApunteRepository apunteRepository) {
        this.apunteRepository = apunteRepository;
    }

    // Obtener los apuntes de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Apunte> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return apunteRepository.findByUsuarioId(usuarioId);
    }

    // Obtener un apunte por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Apunte> obtenerPorId(
            @PathVariable Long id) {

        return apunteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo apunte
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Apunte crear(@RequestBody Apunte apunte) {

        apunte.setId(null);
        return apunteRepository.save(apunte);
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
