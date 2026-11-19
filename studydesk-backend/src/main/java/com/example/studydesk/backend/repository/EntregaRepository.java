
package com.example.studydesk.backend.repository;

import com.example.studydesk.backend.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    List<Entrega> findByUsuarioId(Integer usuarioId);

}
