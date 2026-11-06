
package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.AgendamentoRequestDto;
import com.joaovictor.gustavobarber.dto.AgendamentoResponseDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeRequestDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeResponseDto;
import com.joaovictor.gustavobarber.service.AgendamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDto> cadastrar(
            @Valid @RequestBody AgendamentoRequestDto dto) {

        AgendamentoResponseDto agendamento = agendamentoService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    @PostMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeResponseDto> consultarDisponibilidade(
            @Valid @RequestBody DisponibilidadeRequestDto dto) {

        return ResponseEntity.ok(
                agendamentoService.consultarDisponibilidade(dto)
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDto> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDto>> listarTodos() {

        return ResponseEntity.ok(agendamentoService.listarTodos());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {

        agendamentoService.cancelar(id);

        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id) {

        agendamentoService.concluir(id);

        return ResponseEntity.noContent().build();
    }
}
