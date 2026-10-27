package school.cesar.praxis.domain.acesso;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.List;

/**
 * Raiz de agregado: pedido de um advogado para ler os autos de um processo
 * sigiloso ao qual ainda nao tem acesso. Maquina de estados simples -
 * PENDENTE e o unico estado de onde se sai, para APROVADA, NEGADA (so o
 * chefe) ou CANCELADA (so o proprio solicitante); as demais transicoes sao
 * recusadas pelo dominio.
 */
public class SolicitacaoAcesso {

    public static final int JUSTIFICATIVA_MINIMA = 20;

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String oabSolicitante;
    private final String justificativa;
    private final LocalDate solicitadoEm;
    private StatusSolicitacao status;
    private LocalDate decididoEm;
    private String decididoPorOab;
    private String motivoNegacao;

    /** Solicitacao nova. */
    public SolicitacaoAcesso(NumeroCnj numeroProcesso, String oabSolicitante, String justificativa,
                             LocalDate solicitadoEm) {
        this(null, numeroProcesso, oabSolicitante, justificativa, solicitadoEm,
                StatusSolicitacao.PENDENTE, null, null, null);
    }

    /** Reconstituicao a partir da persistencia. */
    public SolicitacaoAcesso(Long id, NumeroCnj numeroProcesso, String oabSolicitante, String justificativa,
                             LocalDate solicitadoEm, StatusSolicitacao status, LocalDate decididoEm,
                             String decididoPorOab, String motivoNegacao) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("solicitação de acesso exige o processo");
        }
        if (oabSolicitante == null || oabSolicitante.isBlank()) {
            throw new IllegalArgumentException("solicitação de acesso exige a OAB do solicitante");
        }
        if (justificativa == null || justificativa.trim().length() < JUSTIFICATIVA_MINIMA) {
            throw new IllegalArgumentException(
                    "justificativa deve ter ao menos " + JUSTIFICATIVA_MINIMA + " caracteres");
        }
        if (solicitadoEm == null) {
            throw new IllegalArgumentException("data da solicitação é obrigatória");
        }
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.oabSolicitante = oabSolicitante;
        this.justificativa = justificativa.trim();
        this.solicitadoEm = solicitadoEm;
        this.status = status;
        this.decididoEm = decididoEm;
        this.decididoPorOab = decididoPorOab;
        this.motivoNegacao = motivoNegacao;
    }

    public void aprovar(String oabChefe, LocalDate hoje) {
        exigirPendente("aprovar");
        this.status = StatusSolicitacao.APROVADA;
        this.decididoPorOab = oabChefe;
        this.decididoEm = hoje;
    }

    public void negar(String oabChefe, String motivo, LocalDate hoje) {
        exigirPendente("negar");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("motivo da negativa é obrigatório");
        }
        this.status = StatusSolicitacao.NEGADA;
        this.decididoPorOab = oabChefe;
        this.motivoNegacao = motivo;
        this.decididoEm = hoje;
    }

    public void cancelar(LocalDate hoje) {
        exigirPendente("cancelar");
        this.status = StatusSolicitacao.CANCELADA;
        this.decididoEm = hoje;
    }

    private void exigirPendente(String acao) {
        if (status != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException(
                    "só é possível " + acao + " uma solicitação pendente; esta está " + status);
        }
    }

    /**
     * Ja existe solicitacao pendente desta OAB para o mesmo processo? Funcao
     * pura sobre a lista ja carregada pelo caso de uso - nao toca persistencia.
     */
    public static boolean existePendenteDaMesmaOab(List<SolicitacaoAcesso> solicitacoesDoProcesso, String oab) {
        return solicitacoesDoProcesso.stream()
                .anyMatch(s -> s.status == StatusSolicitacao.PENDENTE && s.oabSolicitante.equals(oab));
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getOabSolicitante() {
        return oabSolicitante;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public LocalDate getSolicitadoEm() {
        return solicitadoEm;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public LocalDate getDecididoEm() {
        return decididoEm;
    }

    public String getDecididoPorOab() {
        return decididoPorOab;
    }

    public String getMotivoNegacao() {
        return motivoNegacao;
    }
}
