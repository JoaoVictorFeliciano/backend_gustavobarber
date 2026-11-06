package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.AgendamentoRequestDto;
import com.joaovictor.gustavobarber.dto.AgendamentoResponseDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeRequestDto;
import com.joaovictor.gustavobarber.dto.DisponibilidadeResponseDto;
import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RegraNegocioException;
import com.joaovictor.gustavobarber.exception.RecursoNaoEncontradoException;
import com.joaovictor.gustavobarber.model.Agendamento;
import com.joaovictor.gustavobarber.model.Cliente;
import com.joaovictor.gustavobarber.model.Servico;
import com.joaovictor.gustavobarber.repository.AgendamentoRepository;
import com.joaovictor.gustavobarber.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
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

        List<LocalTime> horariosDisponiveis = new ArrayList<>();

        adicionarHorariosDisponiveis(
                horariosDisponiveis,
                dto.getData(),
                servico,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0)
        );

        adicionarHorariosDisponiveis(
                horariosDisponiveis,
                dto.getData(),
                servico,
                LocalTime.of(13, 0),
                LocalTime.of(18, 0)
        );

        return new DisponibilidadeResponseDto(horariosDisponiveis);
    }

    // Gerar horários disponíveis em intervalos de 30 minutos
    private void adicionarHorariosDisponiveis(
            List<LocalTime> horariosDisponiveis,
            LocalDate data,
            Servico servico,
            LocalTime inicio,
            LocalTime fim) {

        LocalTime horario = inicio;

        while (!horario.plusMinutes(servico.getDuracaoMinutos()).isAfter(fim)) {

            boolean horarioValido = true;

            if (data.equals(LocalDate.now())
                    && !horario.isAfter(LocalTime.now())) {

                horarioValido = false;
            }

            if (horarioValido && !existeConflito(
                    data,
                    horario,
                    servico.getDuracaoMinutos())) {

                horariosDisponiveis.add(horario);
            }

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

    // Listar todos os agendamentos
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDto> listarTodos() {

        return agendamentoRepository.findAll()
                .stream()
                .map(this::converterParaResponseDto)
                .toList();
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
