
package com.joaovictor.gustavobarber.repository;

import com.joaovictor.gustavobarber.model.Agendamento;
import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByData(LocalDate data);

    boolean existsByServico_Id(Long servicoId);

    @Query("""
        SELECT a FROM Agendamento a
        JOIN a.cliente c
        JOIN a.servico s
        WHERE
            (
                :pesquisa IS NULL OR :pesquisa = ''
                OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :pesquisa, '%'))
                OR c.telefone LIKE CONCAT('%', :pesquisa, '%')
                OR LOWER(s.nome) LIKE LOWER(CONCAT('%', :pesquisa, '%'))
            )
            AND (:status IS NULL OR a.status = :status)
            AND (:dataInicio IS NULL OR a.data >= :dataInicio)
            AND (:dataFim IS NULL OR a.data <= :dataFim)
        """)
    Page<Agendamento> buscarComFiltros(
            @Param("pesquisa") String pesquisa,
            @Param("status") StatusAgendamento status,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable
    );
}
