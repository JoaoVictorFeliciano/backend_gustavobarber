package com.joaovictor.gustavobarber.repository;

import com.joaovictor.gustavobarber.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>,
        JpaSpecificationExecutor<Agendamento> {

    List<Agendamento> findByData(LocalDate data);

    boolean existsByServico_Id(Long servicoId);
}
