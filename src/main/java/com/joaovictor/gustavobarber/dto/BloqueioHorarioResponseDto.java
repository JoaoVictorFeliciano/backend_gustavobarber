
package com.joaovictor.gustavobarber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class BloqueioHorarioResponseDto {

    private Long id;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;

}
