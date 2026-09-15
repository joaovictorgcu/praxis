package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.domain.agenda.Audiencia;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;
import school.cesar.praxis.domain.agenda.HorarioInvalidoException;
import school.cesar.praxis.infrastructure.persistence.AudienciaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de Aplicação para a Agenda de Audiências.
 * Implementa os casos de uso definidos no port.
 * Responsabilidades:
 * - Orquestrar operações entre camadas
 * - Validar regras de negócio
 * - Coordenar transações
 */
@Service
@Transactional
public class AgendaDeAudienciasAppService implements AgendaDeAudienciasUseCase {

    private final AudienciaRepository audienciaRepository;

    public AgendaDeAudienciasAppService(AudienciaRepository audienciaRepository) {
        this.audienciaRepository = audienciaRepository;
    }

    @Override
    public AudienciaResponse criarAudiencia(CriarAudienciaRequest request) {
        // Criar nova audiência com dados do request
        Audiencia novaAudiencia = new Audiencia(
            request.getNumeroProcesso(),
            request.getNomeParteAutora(),
            request.getDataHoraInicio(),
            request.getDataHoraFim(),
            request.getSala()
        );
        novaAudiencia.setObservacoes(request.getObservacoes());

        // Verificar conflitos de horário
        verificarConflitosDeHorario(novaAudiencia);

        // Persistir no banco
        Audiencia audienciaSalva = audienciaRepository.save(novaAudiencia);

        return converterParaResponse(audienciaSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public AudienciaResponse consultarAudiencia(Long id) {
        Audiencia audiencia = audienciaRepository.findById(id)
            .filter(Audiencia::isAtiva)
            .orElse(null);
        
        return audiencia != null ? converterParaResponse(audiencia) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public AudienciaResponse consultarAudienciaPorProcesso(String numeroProcesso) {
        Audiencia audiencia = audienciaRepository.findByNumeroProcessoAndAtivaTrue(numeroProcesso)
            .orElse(null);
        
        return audiencia != null ? converterParaResponse(audiencia) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudiencias() {
        return audienciaRepository.findByAtivaTrue()
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudienciasPorSala(String sala) {
        return audienciaRepository.findBySalaAndAtivaTrueOrderByDataHoraInicio(sala)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> listarAudienciasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return audienciaRepository.encontrarPorPeriodo(dataInicio, dataFim)
            .stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    @Override
    public AudienciaResponse editarAudiencia(Long id, CriarAudienciaRequest request) {
        Audiencia audiencia = audienciaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));

        // Edição parcial: campo ausente na requisição mantém o valor atual.
        // O número do processo não muda em edição.
        String nomeParteAutora = request.getNomeParteAutora() != null
            ? request.getNomeParteAutora() : audiencia.getNomeParteAutora();
        LocalDateTime dataHoraInicio = request.getDataHoraInicio() != null
            ? request.getDataHoraInicio() : audiencia.getDataHoraInicio();
        LocalDateTime dataHoraFim = request.getDataHoraFim() != null
            ? request.getDataHoraFim() : audiencia.getDataHoraFim();
        String sala = request.getSala() != null ? request.getSala() : audiencia.getSala();
        String observacoes = request.getObservacoes() != null
            ? request.getObservacoes() : audiencia.getObservacoes();

        // Verificar se há conflitos (excluindo a própria audiência)
        List<Audiencia> conflitos = audienciaRepository.encontrarConflitosDeHorario(
            sala,
            dataHoraInicio,
            dataHoraFim,
            id // Exclui a própria audiência
        );

        if (!conflitos.isEmpty()) {
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("Conflito detectado ao editar audiência. ");
            mensagem.append("Audiências em conflito: ");
            conflitos.forEach(c -> mensagem.append(c.getNumeroProcesso()).append(" "));
            throw new ConflitoDEAudienciaException(mensagem.toString());
        }

        // Atualizar a audiência
        audiencia.atualizar(
            nomeParteAutora,
            dataHoraInicio,
            dataHoraFim,
            sala,
            observacoes
        );

        Audiencia audienciaAtualizada = audienciaRepository.save(audiencia);
        return converterParaResponse(audienciaAtualizada);
    }

    @Override
    public void deletarAudiencia(Long id) {
        Audiencia audiencia = audienciaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));
        
        audiencia.desativar();
        audienciaRepository.save(audiencia);
    }

    @Override
    public void reativarAudiencia(Long id) {
        Audiencia audiencia = audienciaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Audiência não encontrada com ID: " + id));
        
        if (audiencia.isAtiva()) {
            throw new IllegalArgumentException("Audiência já está ativa");
        }
        
        audiencia.reativar();
        audienciaRepository.save(audiencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudienciaResponse> detectarConflitos(CriarAudienciaRequest request) {
        // Com parametro nulo a JPQL compara com NULL e devolve lista vazia: a tela
        // diria "horario livre" para uma consulta invalida.
        if (request.getSala() == null || request.getSala().isBlank()
            || request.getDataHoraInicio() == null || request.getDataHoraFim() == null) {
            throw new IllegalArgumentException("Sala, início e fim são obrigatórios para detectar conflitos");
        }
        if (!request.getDataHoraFim().isAfter(request.getDataHoraInicio())) {
            throw new HorarioInvalidoException("O fim da audiência deve ser posterior ao início");
        }
        List<Audiencia> conflitos = audienciaRepository.encontrarConflitosDeHorario(
            request.getSala(),
            request.getDataHoraInicio(),
            request.getDataHoraFim(),
            0L // ID fictício já que é uma nova audiência
        );

        return conflitos.stream()
            .map(this::converterParaResponse)
            .collect(Collectors.toList());
    }

    // Métodos auxiliares

    /**
     * Verifica se há conflitos de horário para uma audiência
     * @throws ConflitoDEAudienciaException Se houver conflito
     */
    private void verificarConflitosDeHorario(Audiencia novaAudiencia) {
        List<Audiencia> conflitos = audienciaRepository.encontrarConflitosDeHorario(
            novaAudiencia.getSala(),
            novaAudiencia.getDataHoraInicio(),
            novaAudiencia.getDataHoraFim(),
            0L // Não há ID ainda
        );

        if (!conflitos.isEmpty()) {
            StringBuilder mensagem = new StringBuilder();
            mensagem.append("Conflito de horário detectado para a audiência do processo ")
                   .append(novaAudiencia.getNumeroProcesso())
                   .append(" na sala ").append(novaAudiencia.getSala())
                   .append(" entre ").append(novaAudiencia.getDataHoraInicio())
                   .append(" e ").append(novaAudiencia.getDataHoraFim()).append(". ");
            mensagem.append("Audiências em conflito: ");
            conflitos.forEach(a -> mensagem.append(a.getNumeroProcesso()).append(" "));
            
            throw new ConflitoDEAudienciaException(mensagem.toString());
        }
    }

    /**
     * Converte entidade Audiencia para DTO AudienciaResponse
     */
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
            audiencia.getAtualizadaEm()
        );
    }
}
