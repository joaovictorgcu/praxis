package school.cesar.praxis.domain.acesso;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Raiz de agregado do subdominio de apoio <b>Controle de Acesso</b>: concessao
 * de leitura de TODOS os autos (pecas e anexos, atuais e futuros) de um
 * processo sigiloso a uma OAB. A lista de OABs habilitadas por peca/anexo
 * continua existindo e vale junto - esta e a concessao no nivel do processo
 * inteiro, usada pela {@link school.cesar.praxis.domain.compartilhado.PoliticaDeLeituraDosAutos}.
 *
 * <p>Pode ser permanente (sem {@code expiraEm}) ou temporaria. "Ativo" e
 * sempre calculado contra o "hoje" recebido por parametro - nunca
 * {@code LocalDate.now()} - para o teste poder controlar o tempo.
 */
public class AcessoAoProcesso {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String oab;
    private final String concedidoPor;
    private final LocalDate concedidoEm;
    private final LocalDate expiraEm;
    private LocalDate revogadoEm;
    private String motivoRevogacao;

    /** Concessao nova. */
    public AcessoAoProcesso(NumeroCnj numeroProcesso, String oab, String concedidoPor,
                            LocalDate concedidoEm, LocalDate expiraEm) {
        this(null, numeroProcesso, oab, concedidoPor, concedidoEm, expiraEm, null, null);
    }

    /** Reconstituicao a partir da persistencia. */
    public AcessoAoProcesso(Long id, NumeroCnj numeroProcesso, String oab, String concedidoPor,
                            LocalDate concedidoEm, LocalDate expiraEm, LocalDate revogadoEm,
                            String motivoRevogacao) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("acesso ao processo exige o processo");
        }
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("acesso ao processo exige a OAB beneficiada");
        }
        if (concedidoPor == null || concedidoPor.isBlank()) {
            throw new IllegalArgumentException("acesso ao processo exige quem concedeu");
        }
        if (concedidoEm == null) {
            throw new IllegalArgumentException("acesso ao processo exige a data da concessão");
        }
        if (expiraEm != null && expiraEm.isBefore(concedidoEm)) {
            throw new IllegalArgumentException("data de expiração não pode ser anterior à concessão");
        }
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.oab = oab;
        this.concedidoPor = concedidoPor;
        this.concedidoEm = concedidoEm;
        this.expiraEm = expiraEm;
        this.revogadoEm = revogadoEm;
        this.motivoRevogacao = motivoRevogacao;
    }

    /** Nao revogado e, se temporario, ainda nao vencido em {@code hoje}. */
    public boolean ativo(LocalDate hoje) {
        return revogadoEm == null && (expiraEm == null || !hoje.isAfter(expiraEm));
    }

    public boolean expirado(LocalDate hoje) {
        return revogadoEm == null && expiraEm != null && hoje.isAfter(expiraEm);
    }

    /** Dias restantes ate expirar; negativo se ja venceu; vazio se permanente. */
    public Long diasParaExpirar(LocalDate hoje) {
        return expiraEm == null ? null : ChronoUnit.DAYS.between(hoje, expiraEm);
    }

    public void revogar(LocalDate hoje, String motivo) {
        if (revogadoEm != null) {
            throw new IllegalStateException("este acesso já foi revogado");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("motivo da revogação é obrigatório");
        }
        this.revogadoEm = hoje;
        this.motivoRevogacao = motivo;
    }

    /**
     * Um processo sigiloso nunca pode ficar sem nenhum acesso ativo: verdadeiro
     * se, tirando o acesso {@code idARevogar} da conta, ainda sobra pelo menos
     * um ativo entre {@code acessosDoProcesso}. Funcao pura, sem tocar
     * persistencia - quem carrega a lista inteira do processo e o caso de uso.
     */
    public static boolean podeRevogar(List<AcessoAoProcesso> acessosDoProcesso, Long idARevogar, LocalDate hoje) {
        return acessosDoProcesso.stream()
                .filter(a -> !a.getId().equals(idARevogar))
                .anyMatch(a -> a.ativo(hoje));
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getOab() {
        return oab;
    }

    public String getConcedidoPor() {
        return concedidoPor;
    }

    public LocalDate getConcedidoEm() {
        return concedidoEm;
    }

    public LocalDate getExpiraEm() {
        return expiraEm;
    }

    public LocalDate getRevogadoEm() {
        return revogadoEm;
    }

    public String getMotivoRevogacao() {
        return motivoRevogacao;
    }
}
