
package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.AgendamentoRequestDto;
import com.joaovictor.gustavobarber.dto.AgendamentoResponseDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeRequestDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeResponseDto;
import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;
import com.joaovictor.gustavobarber.service.AgendamentoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // Cadastrar agendamento
    @PostMapping
    public ResponseEntity<AgendamentoResponseDto> cadastrar(
            @Valid @RequestBody AgendamentoRequestDto dto) {

        AgendamentoResponseDto agendamento = agendamentoService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(agendamento);
    }

    // Consultar disponibilidade
    @PostMapping("/disponibilidade")
    public ResponseEntity<DisponibilidadeResponseDto> consultarDisponibilidade(
            @Valid @RequestBody DisponibilidadeRequestDto dto) {

        return ResponseEntity.ok(
                agendamentoService.consultarDisponibilidade(dto)
        );
    }

    // Buscar agendamento por ID
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDto> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    // Listar agendamentos com pesquisa, filtros e paginação
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<AgendamentoResponseDto>> listarTodos(

            @RequestParam(required = false) String pesquisa,

            @RequestParam(required = false) StatusAgendamento status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFim,

            @RequestParam(defaultValue = "0") int pagina,

            @RequestParam(defaultValue = "10") int tamanho) {

        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by(Sort.Direction.DESC, "data")
                        .and(Sort.by(Sort.Direction.DESC, "horario"))
        );

        return ResponseEntity.ok(
                agendamentoService.listarTodos(
                        pesquisa,
                        status,
                        dataInicio,
                        dataFim,
                        pageable
                )
        );
    }

    // Cancelar agendamento
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {

        agendamentoService.cancelar(id);

        return ResponseEntity.noContent().build();
    }

    // Concluir agendamento
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id) {

        agendamentoService.concluir(id);

        return ResponseEntity.noContent().build();
    }
}
