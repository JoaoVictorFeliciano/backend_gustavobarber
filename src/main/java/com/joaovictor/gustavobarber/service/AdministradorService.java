package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.LoginRequestDto;
import com.joaovictor.gustavobarber.dto.LoginResponseDto;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RegraNegocioException;
import com.joaovictor.gustavobarber.model.Administrador;
import com.joaovictor.gustavobarber.repository.AdministradorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdministradorService(
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Cadastrar administrador
    @Transactional
    public void cadastrar(String email, String senha) {

        if (administradorRepository.existsByEmail(email)) {
            throw new ConflitoException(
                    "Já existe um administrador cadastrado com esse e-mail.");
        }

        Administrador administrador = new Administrador();

        administrador.setEmail(email);
        administrador.setSenha(passwordEncoder.encode(senha));

        administradorRepository.save(administrador);
    }

    // Realizar login
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto dto) {

        Administrador administrador = administradorRepository
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new RegraNegocioException(
                        "E-mail ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), administrador.getSenha())) {
            throw new RegraNegocioException(
                    "E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(administrador.getEmail());

        return new LoginResponseDto(token, "Bearer");
    }
}