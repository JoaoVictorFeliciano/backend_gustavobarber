
package com.joaovictor.gustavobarber.service;

import com.joaovictor.gustavobarber.dto.BloqueioHorarioRequestDto;
import com.joaovictor.gustavobarber.dto.BloqueioHorarioResponseDto;
import com.joaovictor.gustavobarber.enumerate.StatusAgendamento;
import com.joaovictor.gustavobarber.exception.ConflitoException;
import com.joaovictor.gustavobarber.exception.RegraNegocioException;
import com.joaovictor.gustavobarber.exception.RecursoNaoEncontradoException;
import com.joaovictor.gustavobarber.model.Agendamento;
import com.joaovictor.gustavobarber.model.BloqueioHorario;
import com.joaovictor.gustavobarber.repository.AgendamentoRepository;
import com.joaovictor.gustavobarber.repository.BloqueioHorarioRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class BloqueioHorarioService {

    private final BloqueioHorarioRepository bloqueioHorarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final JdbcTemplate jdbcTemplate;

    public BloqueioHorarioService(
            BloqueioHorarioRepository bloqueioHorarioRepository,
            AgendamentoRepository agendamentoRepository,
            JdbcTemplate jdbcTemplate) {

        this.bloqueioHorarioRepository = bloqueioHorarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Cadastrar bloqueio de horário
    @Transactional
    public BloqueioHorarioResponseDto cadastrar(
            BloqueioHorarioRequestDto dto) {

        validarBloqueio(dto);

        // Utiliza o mesmo bloqueio de transação dos agendamentos
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(987654321)");

        validarConflitoComAgendamentos(
                dto.getData(),
                dto.getHorarioInicio(),
                dto.getHorarioFim()
        );

        validarConflitoComBloqueios(
                dto.getData(),
                dto.getHorarioInicio(),
                dto.getHorarioFim()
        );

        BloqueioHorario bloqueio = new BloqueioHorario();

        bloqueio.setData(dto.getData());
        bloqueio.setHorarioInicio(dto.getHorarioInicio());
        bloqueio.setHorarioFim(dto.getHorarioFim());

        return converterParaResponseDto(
                bloqueioHorarioRepository.save(bloqueio)
        );
    }

    // Listar bloqueios de uma data
    @Transactional(readOnly = true)
    public List<BloqueioHorarioResponseDto> listarPorData(LocalDate data) {

        return bloqueioHorarioRepository.findByData(data)
                .stream()
                .sorted(Comparator.comparing(
                        BloqueioHorario::getHorarioInicio
                ))
                .map(this::converterParaResponseDto)
                .toList();
    }

    // Remover bloqueio e liberar novamente os horários
    @Transactional
    public void remover(Long id) {

        BloqueioHorario bloqueio = bloqueioHorarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Bloqueio de horário não encontrado."
                        ));

        bloqueioHorarioRepository.delete(bloqueio);
    }

    // Validar os dados do bloqueio
    private void validarBloqueio(BloqueioHorarioRequestDto dto) {

        if (dto.getData() == null
                || dto.getHorarioInicio() == null
                || dto.getHorarioFim() == null) {

            throw new RegraNegocioException(
                    "Data, horário inicial e horário final são obrigatórios."
            );
        }

        LocalDate data = dto.getData();
        LocalTime inicio = dto.getHorarioInicio();
        LocalTime fim = dto.getHorarioFim();

        if (data.isBefore(LocalDate.now())) {
            throw new RegraNegocioException(
                    "Não é possível bloquear uma data passada."
            );
        }

        if (data.getDayOfWeek().getValue() == 7) {
            throw new RegraNegocioException(
                    "A barbearia não funciona aos domingos."
            );
        }

        if (!inicio.isBefore(fim)) {
            throw new RegraNegocioException(
                    "O horário final deve ser posterior ao horário inicial."
            );
        }

        if (inicio.getSecond() != 0 || inicio.getNano() != 0
                || fim.getSecond() != 0 || fim.getNano() != 0) {

            throw new RegraNegocioException(
                    "Horário inválido."
            );
        }

        if (inicio.getMinute() % 30 != 0
                || fim.getMinute() % 30 != 0) {

            throw new RegraNegocioException(
                    "Os horários devem ser definidos de meia em meia hora."
            );
        }

        boolean periodoManha =
                !inicio.isBefore(LocalTime.of(8, 0))
                        && !fim.isAfter(LocalTime.of(12, 0));

        boolean periodoTarde =
                !inicio.isBefore(LocalTime.of(13, 0))
                        && !fim.isAfter(LocalTime.of(18, 0));

        if (!periodoManha && !periodoTarde) {
            throw new RegraNegocioException(
                    "O bloqueio deve estar dentro do horário de funcionamento."
            );
        }
    }

    // Verificar conflito com agendamentos existentes
    private void validarConflitoComAgendamentos(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim) {

        List<Agendamento> agendamentos =
                agendamentoRepository.findByData(data);

        boolean existeAgendamento = agendamentos.stream()
                .filter(agendamento ->
                        agendamento.getStatus()
                                != StatusAgendamento.CANCELADO)
                .anyMatch(agendamento -> {

                    LocalTime inicioAgendamento = agendamento.getHorario();

                    LocalTime fimAgendamento =
                            inicioAgendamento.plusMinutes(
                                    agendamento.getServico().getDuracaoMinutos()
                            );

                    return inicio.isBefore(fimAgendamento)
                            && fim.isAfter(inicioAgendamento);
                });

        if (existeAgendamento) {
            throw new ConflitoException(
                    "Não é possível bloquear esse período porque existe "
                            + "um agendamento ativo nele."
            );
        }
    }

    // Impedir sobreposição entre bloqueios
    private void validarConflitoComBloqueios(
            LocalDate data,
            LocalTime inicio,
            LocalTime fim) {

        boolean existeBloqueio = bloqueioHorarioRepository.findByData(data)
                .stream()
                .anyMatch(bloqueio ->
                        inicio.isBefore(bloqueio.getHorarioFim())
                                && fim.isAfter(bloqueio.getHorarioInicio())
                );

        if (existeBloqueio) {
            throw new ConflitoException(
                    "Já existe um bloqueio nesse período."
            );
        }
    }

    // Converter entidade para DTO de resposta
    private BloqueioHorarioResponseDto converterParaResponseDto(
            BloqueioHorario bloqueio) {

        return new BloqueioHorarioResponseDto(
                bloqueio.getId(),
                bloqueio.getData(),
                bloqueio.getHorarioInicio(),
                bloqueio.getHorarioFim()
        );
    }
}
