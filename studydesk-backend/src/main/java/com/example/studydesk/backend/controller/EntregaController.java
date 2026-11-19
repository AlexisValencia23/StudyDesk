
package com.example.studydesk.backend.controller;

import com.example.studydesk.backend.model.Entrega;
import com.example.studydesk.backend.repository.EntregaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    // GET: Obtener entregas por ID de usuario
    // Se mantiene para compatibilidad
    @GetMapping("/usuario/{usuarioId}")
    public List<Entrega> obtenerPorUsuario(
            @PathVariable Integer usuarioId) {

        return entregaRepository.findByUsuarioId(usuarioId);
    }

    // GET: Obtener entregas por correo electronico
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

    // GET: Obtener una entrega por ID
    @GetMapping("/{id}")
    public ResponseEntity<Entrega> obtenerPorId(
            @PathVariable Long id) {

        return entregaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // POST: Crear una entrega
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Entrega crear(@RequestBody Entrega entrega) {

        entrega.setId(null);

        return entregaRepository.save(entrega);
    }

    // PUT: Editar una entrega
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

    // DELETE: Eliminar una entrega
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
