package com.joaovictor.gustavobarber.repository;

import com.joaovictor.gustavobarber.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}