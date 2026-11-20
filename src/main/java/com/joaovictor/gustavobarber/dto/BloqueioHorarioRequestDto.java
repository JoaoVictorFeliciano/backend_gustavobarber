
package com.joaovictor.gustavobarber.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BloqueioHorarioRequestDto {

    @NotNull(message = "A data é obrigatória.")
    private LocalDate data;

    @NotNull(message = "O horário inicial é obrigatório.")
    private LocalTime horarioInicio;

    @NotNull(message = "O horário final é obrigatório.")
    private LocalTime horarioFim;

}
