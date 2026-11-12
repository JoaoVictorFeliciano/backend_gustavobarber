package com.joaovictor.gustavobarber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HorarioDisponibilidadeDto {

    private LocalTime horario;
    private boolean disponivel;
}