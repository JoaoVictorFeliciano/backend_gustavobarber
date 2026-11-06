package com.joaovictor.gustavobarber.dto;

import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AgendamentoResponseDto {

    private Long id;

    private String nomeCliente;

    private String telefoneCliente;

    private String nomeServico;

    private LocalDate data;

    private LocalTime horario;

    private StatusAgendamento status;
}
