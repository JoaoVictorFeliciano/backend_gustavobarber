package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.ServicoRequestDto;
import com.joaovictor.gustavobarber.dto.ServicoResponseDto;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RecursoNaoEncontradoException;
import com.joaovictor.gustavobarber.model.Servico;
import com.joaovictor.gustavobarber.repository.ServicoRepository;
import com.joaovictor.gustavobarber.repository.AgendamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ServicoService(
            ServicoRepository servicoRepository,
            AgendamentoRepository agendamentoRepository) {

        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // Cadastrar serviço
    @Transactional
    public ServicoResponseDto cadastrar(ServicoRequestDto dto) {

        Servico servico = new Servico();

        servico.setNome(dto.getNome().trim());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());

        Servico servicoSalvo = servicoRepository.save(servico);

        return converterParaResponseDto(servicoSalvo);
    }

    // Buscar serviço pelo ID
    @Transactional(readOnly = true)
    public ServicoResponseDto buscarPorId(Long id) {

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Serviço não encontrado."));

        return converterParaResponseDto(servico);
    }

    // Listar todos os serviços
    @Transactional(readOnly = true)
    public List<ServicoResponseDto> listarTodos() {

        return servicoRepository.findAll()
                .stream()
                .map(this::converterParaResponseDto)
                .toList();
    }

    // Atualizar serviço
    @Transactional
    public ServicoResponseDto atualizar(Long id, ServicoRequestDto dto) {

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Serviço não encontrado."));

        servico.setNome(dto.getNome().trim());
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos());

        Servico servicoAtualizado = servicoRepository.save(servico);

        return converterParaResponseDto(servicoAtualizado);
    }

    // Excluir serviço
    @Transactional
    public void excluir(Long id) {

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Serviço não encontrado."));

        if (agendamentoRepository.existsByServico_Id(id)) {
            throw new ConflitoException(
                    "Não é possível excluir um serviço que possui agendamentos associados.");
        }

        servicoRepository.delete(servico);
    }

    // Converter entidade para DTO de resposta
    private ServicoResponseDto converterParaResponseDto(Servico servico) {

        ServicoResponseDto dto = new ServicoResponseDto();

        dto.setId(servico.getId());
        dto.setNome(servico.getNome());
        dto.setDescricao(servico.getDescricao());
        dto.setPreco(servico.getPreco());
        dto.setDuracaoMinutos(servico.getDuracaoMinutos());

        return dto;
    }
}