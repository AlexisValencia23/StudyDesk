
package com.example.studydesk.backend.repository;

import com.example.studydesk.backend.model.Apunte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApunteRepository extends JpaRepository<Apunte, Long> {

    List<Apunte> findByUsuarioId(Integer usuarioId);

}
