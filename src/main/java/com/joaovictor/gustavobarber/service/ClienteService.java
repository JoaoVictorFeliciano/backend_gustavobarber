package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.ClienteRequestDto;
import com.joaovictor.gustavobarber.dto.ClienteResponseDto;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RecursoNaoEncontradoException;
import com.joaovictor.gustavobarber.model.Cliente;
import com.joaovictor.gustavobarber.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // Cadastrar um novo cliente
    @Transactional
    public ClienteResponseDto cadastrar(ClienteRequestDto dto) {

        String telefone = normalizarTelefone(dto.getTelefone());

        if (clienteRepository.findByTelefone(telefone).isPresent()) {
            throw new ConflitoException("Já existe um cliente cadastrado com esse telefone.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(telefone);

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaResponseDto(clienteSalvo);
    }

    // Buscar cliente pelo ID
    @Transactional(readOnly = true)
    public ClienteResponseDto buscarPorId(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));

        return converterParaResponseDto(cliente);
    }

    // Buscar cliente pelo telefone
    @Transactional(readOnly = true)
    public ClienteResponseDto buscarPorTelefone(String telefone) {

        String telefoneNormalizado = normalizarTelefone(telefone);

        Cliente cliente = clienteRepository.findByTelefone(telefoneNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));

        return converterParaResponseDto(cliente);
    }

    // Buscar cliente existente ou cadastrar um novo
    @Transactional
    public Cliente buscarOuCadastrar(String nome, String telefone) {

        String telefoneNormalizado = normalizarTelefone(telefone);

        return clienteRepository.findByTelefone(telefoneNormalizado)
                .orElseGet(() -> {

                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(nome.trim());
                    novoCliente.setTelefone(telefoneNormalizado);

                    return clienteRepository.save(novoCliente);
                });
    }

    // Atualizar os dados de um cliente
    @Transactional
    public ClienteResponseDto atualizar(Long id, ClienteRequestDto dto) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));

        String telefoneNormalizado = normalizarTelefone(dto.getTelefone());

        clienteRepository.findByTelefone(telefoneNormalizado)
                .filter(outroCliente -> !outroCliente.getId().equals(id))
                .ifPresent(outroCliente -> {
                    throw new ConflitoException("Esse telefone já pertence a outro cliente.");
                });

        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(telefoneNormalizado);

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return converterParaResponseDto(clienteAtualizado);
    }

    // Listar todos os clientes
    @Transactional(readOnly = true)
    public List<ClienteResponseDto> listarTodos() {

        return clienteRepository.findAll()
                .stream()
                .map(this::converterParaResponseDto)
                .toList();
    }

    // Remover caracteres não numéricos do telefone
    private String normalizarTelefone(String telefone) {
        return telefone.replaceAll("\\D", "");
    }

    // Converter entidade para DTO de resposta
    private ClienteResponseDto converterParaResponseDto(Cliente cliente) {

        ClienteResponseDto dto = new ClienteResponseDto();

        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setTelefone(cliente.getTelefone());

        return dto;
    }
}