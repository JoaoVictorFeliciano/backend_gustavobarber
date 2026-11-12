package com.joaovictor.gustavobarber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DisponibilidadeResponseDto {

    private List<HorarioDisponibilidadeDto> horarios;
}