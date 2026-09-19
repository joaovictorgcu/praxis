package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.application.port.out.AudienciaRepositorio;
import school.cesar.praxis.domain.agenda.AgendaDeAudiencias;
import school.cesar.praxis.domain.agenda.Audiencia;
import school.cesar.praxis.domain.agenda.HorarioInvalidoException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AgendaDeAudienciasAppService implements AgendaDeAudienciasUseCase {

    private final AudienciaRepositorio audiencias;

    public AgendaDeAudienciasAppService(AudienciaRepositorio audiencias) {
        this.audiencias = audiencias;
    }

    @Override
    public AudienciaResponse criarAudiencia(CriarAudienciaRequest request) {
        garantirHorarioValido(request.getDataHoraInicio(), request.getDataHoraFim());
        Audiencia nova = new Audiencia(
                request.getNumeroProcesso(),
                request.getNomeParteAutora(),
                request.getDataHoraInicio(),
                request.getDataHoraFim(),
                request.getSala());
        nova.setObservacoes(request.getObservacoes());
        AgendaDeAudiencias.de(audiencias.porSalaAtivas(nova.getSala())).garantirSemConflito(nova);
        return converterParaResponse(audiencias.salvar(nova));
    }

    @Override
    @Transactional(readOnly = true)
    public AudienciaResponse consultarAudiencia(Long id) {
        return audiencias.porId(id)
                .filter(Audiencia::isAtiva)
                .map(this::converterParaResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AudienciaResponse consultarAudienciaPorProcesso(String numeroProcesso) {
        return audiencias.porNumeroProcessoAtiva(numeroProcesso)
                .map(this::converterParaResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudiencias() {
        return audiencias.listarAtivas().stream().map(this::converterParaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarTodasAsAudiencias() {
        return audiencias.listarTodas().stream().map(this::converterParaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudienciasPorSala(String sala) {
        return audiencias.porSalaAtivas(sala).stream().map(this::converterParaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudienciasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return audiencias.porPeriodoAtivas(dataInicio, dataFim).stream()
                .map(this::converterParaResponse).toList();
    }

    @Override
    public AudienciaResponse editarAudiencia(Long id, CriarAudienciaRequest request) {
        Audiencia audiencia = audiencias.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));

        String nomeParteAutora = request.getNomeParteAutora() != null
                ? request.getNomeParteAutora() : audiencia.getNomeParteAutora();
        LocalDateTime dataHoraInicio = request.getDataHoraInicio() != null
                ? request.getDataHoraInicio() : audiencia.getDataHoraInicio();
        LocalDateTime dataHoraFim = request.getDataHoraFim() != null
                ? request.getDataHoraFim() : audiencia.getDataHoraFim();
        String sala = request.getSala() != null ? request.getSala() : audiencia.getSala();
        String observacoes = request.getObservacoes() != null
                ? request.getObservacoes() : audiencia.getObservacoes();

        garantirHorarioValido(dataHoraInicio, dataHoraFim);
        audiencia.atualizar(nomeParteAutora, dataHoraInicio, dataHoraFim, sala, observacoes);
        AgendaDeAudiencias.de(audiencias.porSalaAtivas(sala)).garantirSemConflito(audiencia);
        return converterParaResponse(audiencias.salvar(audiencia));
    }

    @Override
    public void deletarAudiencia(Long id) {
        Audiencia audiencia = audiencias.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));
        audiencia.desativar();
        audiencias.salvar(audiencia);
    }

    @Override
    public void reativarAudiencia(Long id) {
        Audiencia audiencia = audiencias.porId(id)
                .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));
        if (audiencia.isAtiva()) {
            throw new IllegalArgumentException("Audiência já está ativa");
        }
        audiencia.reativar();
        audiencias.salvar(audiencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> detectarConflitos(CriarAudienciaRequest request) {
        if (request.getSala() == null || request.getSala().isBlank()
                || request.getDataHoraInicio() == null || request.getDataHoraFim() == null) {
            throw new IllegalArgumentException("Sala, início e fim são obrigatórios para detectar conflitos");
        }
        garantirHorarioValido(request.getDataHoraInicio(), request.getDataHoraFim());
        Audiencia sonda = new Audiencia(
                null,
                request.getNumeroProcesso(),
                request.getNomeParteAutora(),
                request.getDataHoraInicio(),
                request.getDataHoraFim(),
                request.getSala(),
                request.getObservacoes(),
                true,
                request.getDataHoraInicio(),
                null);
        return AgendaDeAudiencias.de(audiencias.porSalaAtivas(request.getSala()))
                .encontrarConflitos(sonda)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private static void garantirHorarioValido(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Data e hora de início e fim não podem ser nulas");
        }
        if (!fim.isAfter(inicio)) {
            throw new HorarioInvalidoException(
                    "A hora final da audiência deve ser posterior à hora inicial");
        }
    }

    private AudienciaResponse converterParaResponse(Audiencia audiencia) {
        return new AudienciaResponse(
                audiencia.getId(),
                audiencia.getNumeroProcesso(),
                audiencia.getNomeParteAutora(),
                audiencia.getDataHoraInicio(),
                audiencia.getDataHoraFim(),
                audiencia.getSala(),
                audiencia.getObservacoes(),
                audiencia.isAtiva(),
                audiencia.getCriadaEm(),
                audiencia.getAtualizadaEm());
    }
}
