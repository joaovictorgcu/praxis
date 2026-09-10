package school.cesar.praxis.domain.agenda;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agregado de domínio que representa a agenda de audiências.
 * Responsabilidades:
 * - Gerenciar a coleção de audiências
 * - Detectar conflitos de horário
 * - Validar regras de negócio antes de operações
 *
 * Padrão: Agregado (DDD)
 * A agenda é o agregado raiz que encapsula as audiências.
 */
public class AgendaDeAudiencias {

    private final List<Audiencia> audiencias = new ArrayList<>();

    // Operações de consulta

    /**
     * Lista todas as audiências ativas
     */
    public List<Audiencia> listarTodas() {
        return new ArrayList<>(audiencias.stream()
            .filter(Audiencia::isAtiva)
            .collect(Collectors.toList()));
    }

    /**
     * Busca audiência por ID
     */
    public Audiencia encontrarPorId(Long id) {
        return audiencias.stream()
            .filter(a -> a.getId().equals(id) && a.isAtiva())
            .findFirst()
            .orElse(null);
    }

    /**
     * Busca audiência por número de processo
     */
    public Audiencia encontrarPorNumeroProcesso(String numeroProcesso) {
        return audiencias.stream()
            .filter(a -> a.getNumeroProcesso().equals(numeroProcesso) && a.isAtiva())
            .findFirst()
            .orElse(null);
    }

    /**
     * Lista audiências dentro de um período de datas
     */
    public List<Audiencia> listarPorData(LocalDateTime dataInicio, LocalDateTime dataFim) {
        return audiencias.stream()
            .filter(Audiencia::isAtiva)
            .filter(a -> a.estaContidoEm(dataInicio, dataFim) ||
                        a.getDataHoraInicio().toLocalDate().equals(dataInicio.toLocalDate()))
            .sorted(Comparator.comparing(Audiencia::getDataHoraInicio))
            .collect(Collectors.toList());
    }

    /**
     * Lista audiências de uma sala específica
     */
    public List<Audiencia> listarPorSala(String sala) {
        return audiencias.stream()
            .filter(a -> a.getSala().equals(sala) && a.isAtiva())
            .sorted(Comparator.comparing(Audiencia::getDataHoraInicio))
            .collect(Collectors.toList());
    }

    /**
     * Quantidade total de audiências ativas
     */
    public int quantidadeDeAudiencias() {
        return (int) audiencias.stream()
            .filter(Audiencia::isAtiva)
            .count();
    }

    /**
     * Verifica se a agenda contém a audiência
     */
    public boolean contemAudiencia(Audiencia audiencia) {
        return audiencias.contains(audiencia);
    }

    // Operações de detecção de conflito

    /**
     * Verifica se há conflito de horário para uma audiência
     * Retorna true se a audiência se sobrepõe com alguma outra na mesma sala
     */
    public boolean temConflito(Audiencia novaAudiencia) {
        return audiencias.stream()
            .filter(Audiencia::isAtiva)
            .filter(a -> {
                // Exclui a mesma audiência (se tiver ID) ou a mesma referência de processo
                if (a.getId() != null && novaAudiencia.getId() != null) {
                    return !a.getId().equals(novaAudiencia.getId());
                }
                return !a.getNumeroProcesso().equals(novaAudiencia.getNumeroProcesso());
            })
            .anyMatch(novaAudiencia::temConflitoCom);
    }

    /**
     * Lista todas as audiências que conflitam com a audiência fornecida
     */
    public List<Audiencia> encontrarConflitos(Audiencia novaAudiencia) {
        return audiencias.stream()
            .filter(Audiencia::isAtiva)
            .filter(a -> {
                // Exclui a mesma audiência (se tiver ID) ou a mesma referência de processo
                if (a.getId() != null && novaAudiencia.getId() != null) {
                    return !a.getId().equals(novaAudiencia.getId());
                }
                return !a.getNumeroProcesso().equals(novaAudiencia.getNumeroProcesso());
            })
            .filter(novaAudiencia::temConflitoCom)
            .collect(Collectors.toList());
    }

    // Operações de escrita

    /**
     * Adiciona uma nova audiência à agenda
     * Lança exceção se houver conflito de horário
     *
     * @param audiencia A audiência a ser adicionada
     * @throws ConflitoDEAudienciaException Se houver conflito de horário
     */
    public void adicionarAudiencia(Audiencia audiencia) {
        if (temConflito(audiencia)) {
            List<Audiencia> conflitos = encontrarConflitos(audiencia);
            String mensagemConflito = montarMensagemConflito(audiencia, conflitos);
            throw new ConflitoDEAudienciaException(mensagemConflito);
        }
        audiencias.add(audiencia);
    }

    /**
     * Atualiza uma audiência existente
     * Lança exceção se a atualização criar conflito de horário
     *
     * @param audienciaAtualizada A audiência com dados atualizados
     * @throws ConflitoDEAudienciaException Se houver conflito de horário
     */
    public void atualizar(Audiencia audienciaAtualizada) {
        Audiencia audienciaExistente = encontrarPorId(audienciaAtualizada.getId());
        
        if (audienciaExistente == null) {
            throw new IllegalArgumentException("Audiência com ID " + audienciaAtualizada.getId() + " não encontrada");
        }

        // Verifica conflitos (excluindo a própria audiência)
        if (temConflito(audienciaAtualizada)) {
            List<Audiencia> conflitos = encontrarConflitos(audienciaAtualizada);
            String mensagemConflito = montarMensagemConflito(audienciaAtualizada, conflitos);
            throw new ConflitoDEAudienciaException(mensagemConflito);
        }

        audienciaExistente.atualizar(
            audienciaAtualizada.getNomeParteAutora(),
            audienciaAtualizada.getDataHoraInicio(),
            audienciaAtualizada.getDataHoraFim(),
            audienciaAtualizada.getSala(),
            audienciaAtualizada.getObservacoes()
        );
    }

    /**
     * Remove (desativa) uma audiência
     */
    public void remover(Long id) {
        Audiencia audiencia = encontrarPorId(id);
        if (audiencia != null) {
            audiencia.desativar();
        }
    }

    /**
     * Reativa uma audiência desativada
     */
    public void reativar(Long id) {
        Audiencia audiencia = audiencias.stream()
            .filter(a -> a.getId().equals(id) && !a.isAtiva())
            .findFirst()
            .orElse(null);
        
        if (audiencia != null) {
            audiencia.reativar();
        }
    }

    // Método auxiliar

    private String montarMensagemConflito(Audiencia novaAudiencia, List<Audiencia> conflitos) {
        StringBuilder sb = new StringBuilder();
        sb.append("Conflito detectado para a audiência do processo ").append(novaAudiencia.getNumeroProcesso())
          .append(" na sala ").append(novaAudiencia.getSala())
          .append(" entre ").append(novaAudiencia.getDataHoraInicio())
          .append(" e ").append(novaAudiencia.getDataHoraFim())
          .append(". Audiências em conflito: ");

        for (Audiencia conflito : conflitos) {
            sb.append("[Proc. ").append(conflito.getNumeroProcesso())
              .append(" - ").append(conflito.getDataHoraInicio())
              .append(" a ").append(conflito.getDataHoraFim()).append("] ");
        }

        return sb.toString();
    }
}
