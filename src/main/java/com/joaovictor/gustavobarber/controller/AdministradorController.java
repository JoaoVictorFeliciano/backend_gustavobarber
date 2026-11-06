package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.LoginRequestDto;
import com.joaovictor.gustavobarber.dto.LoginResponseDto;
import com.joaovictor.gustavobarber.service.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {

        LoginResponseDto resposta = administradorService.login(dto);

        return ResponseEntity.ok(resposta);
    }
}