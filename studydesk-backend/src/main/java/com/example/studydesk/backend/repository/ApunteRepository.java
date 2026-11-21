
package com.example.studydesk.backend.repository;

import com.example.studydesk.backend.model.Apunte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApunteRepository
        extends JpaRepository<Apunte, Long> {

    // Consultar apuntes por usuario local
    List<Apunte> findByUsuarioId(Integer usuarioId);

    // Consultar apuntes por correo
    List<Apunte> findByCorreoUsuarioIgnoreCase(
            String correoUsuario
    );

    // NUEVO: Buscar un apunte por su ID de sincronizacion
    Optional<Apunte> findByIdCliente(String idCliente);
}
