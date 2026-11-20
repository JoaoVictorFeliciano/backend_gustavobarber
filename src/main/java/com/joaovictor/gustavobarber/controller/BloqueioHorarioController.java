
package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.BloqueioHorarioRequestDto;
import com.joaovictor.gustavobarber.dto.BloqueioHorarioResponseDto;
import com.joaovictor.gustavobarber.service.BloqueioHorarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bloqueios-horarios")
@SecurityRequirement(name = "bearerAuth")
public class BloqueioHorarioController {

    private final BloqueioHorarioService bloqueioHorarioService;

    public BloqueioHorarioController(
            BloqueioHorarioService bloqueioHorarioService) {
        this.bloqueioHorarioService = bloqueioHorarioService;
    }

    // Cadastrar bloqueio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueioHorarioResponseDto cadastrar(
            @Valid @RequestBody BloqueioHorarioRequestDto dto) {

        return bloqueioHorarioService.cadastrar(dto);
    }

    // Consultar bloqueios de uma data
    @GetMapping
    public List<BloqueioHorarioResponseDto> listarPorData(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {

        return bloqueioHorarioService.listarPorData(data);
    }

    // Remover bloqueio
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {

        bloqueioHorarioService.remover(id);
    }
}
