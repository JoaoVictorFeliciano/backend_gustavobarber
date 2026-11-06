package com.joaovictor.gustavobarber.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class DisponibilidadeRequestDto {

    @NotNull(message = "A data é obrigatória")
    private LocalDate data;

    @NotNull(message = "O serviço deve ser selecionado")
    @Positive(message = "Serviço inválido")
    private Long servicoId;
}