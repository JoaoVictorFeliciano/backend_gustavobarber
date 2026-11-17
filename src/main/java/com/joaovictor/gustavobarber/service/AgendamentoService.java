
package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.AgendamentoRequestDto;
import com.joaovictor.gustavobarber.dto.AgendamentoResponseDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeRequestDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeResponseDto;
import com.joaovictor.gustavobarber.dto.HorarioDisponibilidadeDto;
import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RegraNegocioException;
import com.joaovictor.gustavobarber.exception.RecursoNaoEncontradoException;
import com.joaovictor.gustavobarber.model.Agendamento;
import com.joaovictor.gustavobarber.model.Cliente;
import com.joaovictor.gustavobarber.model.Servico;
import com.joaovictor.gustavobarber.repository.AgendamentoRepository;
import com.joaovictor.gustavobarber.repository.ServicoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteService clienteService;
    private final JdbcTemplate jdbcTemplate;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ServicoRepository servicoRepository,
            ClienteService clienteService,
            JdbcTemplate jdbcTemplate) {

        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
        this.clienteService = clienteService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Criar um novo agendamento
    @Transactional
    public AgendamentoResponseDto cadastrar(AgendamentoRequestDto dto) {

        validarData(dto.getData());

        Servico servico = buscarServico(dto.getServicoId());

        LocalTime horario = dto.getHorario();

        validarHorarioFuncionamento(horario, servico.getDuracaoMinutos());

        if (dto.getData().equals(LocalDate.now())
                && !horario.isAfter(LocalTime.now())) {

            throw new RegraNegocioException(
                    "Não é possível agendar em um horário que já passou.");
        }

        // Bloquear a agenda durante a criação do agendamento
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(987654321)");

        validarConflito(
                dto.getData(),
                horario,
                servico.getDuracaoMinutos()
        );

        Cliente cliente = clienteService.buscarOuCadastrar(
                dto.getNomeCliente(),
                dto.getTelefoneCliente()
        );

        Agendamento agendamento = new Agendamento();

        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setData(dto.getData());
        agendamento.setHorario(horario);
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        return converterParaResponseDto(agendamentoSalvo);
    }

    // Consultar horários disponíveis
    @Transactional(readOnly = true)
    public DisponibilidadeResponseDto consultarDisponibilidade(
            DisponibilidadeRequestDto dto) {

        validarData(dto.getData());

        Servico servico = buscarServico(dto.getServicoId());

        List<HorarioDisponibilidadeDto> horarios = new ArrayList<>();

        adicionarHorariosDisponiveis(
                horarios,
                dto.getData(),
                servico,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        adicionarHorariosDisponiveis(
                horarios,
                dto.getData(),
                servico,
                LocalTime.of(13, 0),
                LocalTime.of(18, 0)
        );

        return new DisponibilidadeResponseDto(horarios);
    }

    // Gerar horários disponíveis em intervalos de 30 minutos
    private void adicionarHorariosDisponiveis(
            List<HorarioDisponibilidadeDto> horarios,
            LocalDate data,
            Servico servico,
            LocalTime inicio,
            LocalTime fim) {

        LocalTime horario = inicio;

        while (!horario.plusMinutes(servico.getDuracaoMinutos()).isAfter(fim)) {

            if (data.equals(LocalDate.now())
                    && !horario.isAfter(LocalTime.now())) {

                horario = horario.plusMinutes(30);
                continue;
            }

            boolean disponivel = !existeConflito(
                    data,
                    horario,
                    servico.getDuracaoMinutos()
            );

            horarios.add(
                    new HorarioDisponibilidadeDto(horario, disponivel)
            );

            horario = horario.plusMinutes(30);
        }
    }

    // Validar data do agendamento
    private void validarData(LocalDate data) {

        if (data.isBefore(LocalDate.now())) {
            throw new RegraNegocioException(
                    "Não é possível agendar em uma data passada.");
        }

        if (data.getDayOfWeek().getValue() == 7) {
            throw new RegraNegocioException(
                    "A barbearia não funciona aos domingos.");
        }
    }

    // Validar horário de funcionamento
    private void validarHorarioFuncionamento(
            LocalTime horario,
            Integer duracaoMinutos) {

        if (horario == null) {
            throw new RegraNegocioException("O horário é obrigatório.");
        }

        if (horario.getMinute() != 0 && horario.getMinute() != 30) {
            throw new RegraNegocioException(
                    "Os horários devem ser marcados de meia em meia hora.");
        }

        if (horario.getSecond() != 0 || horario.getNano() != 0) {
            throw new RegraNegocioException("Horário inválido.");
        }

        LocalTime fimAtendimento = horario.plusMinutes(duracaoMinutos);

        boolean periodoManha =
                !horario.isBefore(LocalTime.of(8, 0))
                        && !fimAtendimento.isAfter(LocalTime.of(12, 0));

        boolean periodoTarde =
                !horario.isBefore(LocalTime.of(13, 0))
                        && !fimAtendimento.isAfter(LocalTime.of(18, 0));

        if (!periodoManha && !periodoTarde) {
            throw new RegraNegocioException(
                    "Horário fora do funcionamento da barbearia.");
        }
    }

    // Verificar se existe conflito com outro agendamento
    private void validarConflito(
            LocalDate data,
            LocalTime horario,
            Integer duracaoMinutos) {

        if (existeConflito(data, horario, duracaoMinutos)) {
            throw new ConflitoException(
                    "Esse horário não está disponível. Escolha outro horário.");
        }
    }

    private boolean existeConflito(
            LocalDate data,
            LocalTime horario,
            Integer duracaoMinutos) {

        LocalTime fimNovoAgendamento = horario.plusMinutes(duracaoMinutos);

        return agendamentoRepository.findByData(data)
                .stream()
                .filter(agendamento ->
                        agendamento.getStatus() != StatusAgendamento.CANCELADO)
                .anyMatch(agendamento -> {

                    LocalTime inicioExistente = agendamento.getHorario();

                    LocalTime fimExistente = inicioExistente.plusMinutes(
                            agendamento.getServico().getDuracaoMinutos()
                    );

                    return horario.isBefore(fimExistente)
                            && fimNovoAgendamento.isAfter(inicioExistente);
                });
    }

    // Buscar serviço pelo ID
    private Servico buscarServico(Long servicoId) {

        return servicoRepository.findById(servicoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Serviço não encontrado."));
    }

    // Buscar agendamento pelo ID
    @Transactional(readOnly = true)
    public AgendamentoResponseDto buscarPorId(Long id) {

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Agendamento não encontrado."));

        return converterParaResponseDto(agendamento);
    }

    // Listar agendamentos com pesquisa, filtros e paginação
    @Transactional(readOnly = true)
    public Page<AgendamentoResponseDto> listarTodos(
            String pesquisa,
            StatusAgendamento status,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable) {

        if (dataInicio != null && dataFim != null
                && dataInicio.isAfter(dataFim)) {

            throw new RegraNegocioException(
                    "A data inicial não pode ser posterior à data final.");
        }

        Specification<Agendamento> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (pesquisa != null && !pesquisa.isBlank()) {
            String termo = "%" + pesquisa.toLowerCase() + "%";
            String termoTelefone = "%" + pesquisa + "%";

            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("cliente").get("nome").as(String.class)
                                    ),
                                    termo
                            ),
                            criteriaBuilder.like(
                                    root.get("cliente").get("telefone").as(String.class),
                                    termoTelefone
                            ),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("servico").get("nome").as(String.class)
                                    ),
                                    termo
                            )
                    )
            );
        }

        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status)
            );
        }

        if (dataInicio != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(
                            root.get("data").as(LocalDate.class),
                            dataInicio
                    )
            );
        }

        if (dataFim != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(
                            root.get("data").as(LocalDate.class),
                            dataFim
                    )
            );
        }

        return agendamentoRepository.findAll(specification, pageable)
                .map(this::converterParaResponseDto);
    }

    // Cancelar agendamento
    @Transactional
    public void cancelar(Long id) {

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Agendamento não encontrado."));

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new ConflitoException("Esse agendamento já está cancelado.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);

        agendamentoRepository.save(agendamento);
    }

    // Concluir agendamento
    @Transactional
    public void concluir(Long id) {

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Agendamento não encontrado."));

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new ConflitoException(
                    "Não é possível concluir um agendamento cancelado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new ConflitoException(
                    "Esse agendamento já foi concluído.");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);

        agendamentoRepository.save(agendamento);
    }

    // Converter entidade para DTO de resposta
    private AgendamentoResponseDto converterParaResponseDto(
            Agendamento agendamento) {

        AgendamentoResponseDto dto = new AgendamentoResponseDto();

        dto.setId(agendamento.getId());
        dto.setNomeCliente(agendamento.getCliente().getNome());
        dto.setTelefoneCliente(agendamento.getCliente().getTelefone());
        dto.setNomeServico(agendamento.getServico().getNome());
        dto.setData(agendamento.getData());
        dto.setHorario(agendamento.getHorario());
        dto.setStatus(agendamento.getStatus());

        return dto;
    }
}
