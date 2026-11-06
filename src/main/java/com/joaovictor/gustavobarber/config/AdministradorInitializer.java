package com.joaovictor.gustavobarber.config;

import com.joaovictor.gustavobarber.repository.AdministradorRepository;
import com.joaovictor.gustavobarber.service.AdministradorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdministradorInitializer implements CommandLineRunner {

    private final AdministradorService administradorService;
    private final AdministradorRepository administradorRepository;

    @Value("${admin.email}")
    private String email;

    @Value("${admin.password}")
    private String senha;

    public AdministradorInitializer(
            AdministradorService administradorService,
            AdministradorRepository administradorRepository) {

        this.administradorService = administradorService;
        this.administradorRepository = administradorRepository;
    }

    @Override
    public void run(String... args) {

        if (email.isBlank() || senha.isBlank()) {
            throw new IllegalStateException(
                    "Configure ADMIN_EMAIL e ADMIN_PASSWORD.");
        }

        if (administradorRepository.existsByEmail(email)) {
            System.out.println("Administrador já cadastrado.");
            return;
        }

        administradorService.cadastrar(email, senha);

        System.out.println("Administrador inicial cadastrado com sucesso!");
    }
}