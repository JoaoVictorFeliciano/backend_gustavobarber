package com.joaovictor.gustavobarber.repository;

import com.joaovictor.gustavobarber.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByData(LocalDate data);
    boolean existsByServico_Id(Long servicoId);
}
