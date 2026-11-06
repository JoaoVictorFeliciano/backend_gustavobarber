
package com.joaovictor.gustavobarber.controller;

import com.joaovictor.gustavobarber.dto.ClienteRequestDto;
import com.joaovictor.gustavobarber.dto.ClienteResponseDto;
import com.joaovictor.gustavobarber.service.ClienteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // Cadastrar cliente
    @PostMapping
    public ResponseEntity<ClienteResponseDto> cadastrar(
            @Valid @RequestBody ClienteRequestDto dto) {

        ClienteResponseDto cliente = clienteService.cadastrar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    // Buscar cliente pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // Listar todos os clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> listarTodos() {

        return ResponseEntity.ok(clienteService.listarTodos());
    }

    // Atualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDto dto) {

        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }
}
