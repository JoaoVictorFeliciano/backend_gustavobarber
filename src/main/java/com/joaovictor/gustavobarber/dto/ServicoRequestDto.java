package com.joaovictor.gustavobarber.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServicoRequestDto {

    @NotBlank(message = "O nome do serviço é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo duas casas decimais")
    private BigDecimal preco;

    @NotNull(message = "A duração é obrigatória")
    @Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto")
    private Integer duracaoMinutos;

}
