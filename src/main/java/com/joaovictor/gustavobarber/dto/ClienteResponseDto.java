package com.joaovictor.gustavobarber.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteResponseDto {
    private Long id;
    private String nome;
    private String telefone;
}
