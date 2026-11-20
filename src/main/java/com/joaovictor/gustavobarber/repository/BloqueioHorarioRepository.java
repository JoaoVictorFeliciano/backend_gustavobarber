
package com.joaovictor.gustavobarber.repository;

import com.joaovictor.gustavobarber.model.BloqueioHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueioHorarioRepository
        extends JpaRepository<BloqueioHorario, Long> {

    List<BloqueioHorario> findByData(LocalDate data);
}
