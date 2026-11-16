package com.joaovictor.gustavobarber.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AgendamentoRequestDto {

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nomeCliente;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    @Pattern(
            regexp = "^[0-9()+\\-\\s]+$",
            message = "O telefone contém caracteres inválidos"
    )
    private String telefoneCliente;

    @NotNull(message = "A data do agendamento é obrigatória")
    @FutureOrPresent(message = "A data não pode ser anterior à data atual")
    private LocalDate data;

    @NotNull(message = "O horário é obrigatório")
    private LocalTime horario;

    @NotNull(message = "O serviço deve ser selecionado")
    @Positive(message = "O ID do serviço deve ser válido")
    private Long servicoId;
}