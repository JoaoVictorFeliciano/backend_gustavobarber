
package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.ServicoRequestDto;
import com.joaovictor.gustavobarber.dto.ServicoResponseDto;
import com.joaovictor.gustavobarber.service.ServicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ServicoResponseDto> cadastrar(
            @Valid @RequestBody ServicoRequestDto dto) {

        ServicoResponseDto servico = servicoService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(servico);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDto> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponseDto>> listarTodos() {

        return ResponseEntity.ok(servicoService.listarTodos());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDto> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequestDto dto) {

        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        servicoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
