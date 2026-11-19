
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Entrega;
import com.example.studydesk.backend.repository.EntregaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    // Obtener las entregas de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public List<Entrega> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return entregaRepository.findByUsuarioId(usuarioId);
    }

    // Obtener una entrega por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Entrega> obtenerPorId(
            @PathVariable Long id) {

        return entregaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear una nueva entrega
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Entrega crear(@RequestBody Entrega entrega) {

        entrega.setId(null);
        return entregaRepository.save(entrega);
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
