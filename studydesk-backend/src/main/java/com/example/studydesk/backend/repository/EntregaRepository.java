
package com.example.studydesk.backend.repository;

import com.example.studydesk.backend.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntregaRepository
        extends JpaRepository<Entrega, Long> {

    // Consultar entregas por usuario local
    List<Entrega> findByUsuarioId(Integer usuarioId);

    // Consultar entregas por correo
    List<Entrega> findByCorreoUsuarioIgnoreCase(
            String correoUsuario
    );

    // NUEVO: Buscar una entrega por su ID de sincronizacion
    Optional<Entrega> findByIdCliente(String idCliente);
}
