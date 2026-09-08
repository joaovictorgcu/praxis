package school.cesar.praxis.domain.prazo;

import school.cesar.praxis.domain.processo.Advogado;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Prazos &amp; Agenda</b>.
 *
 * <p>Referencia o Processo por identidade ({@link NumeroCnj}) em vez de por
 * objeto: agregados pequenos, consistencia transacional por prazo. O vencimento
 * e calculado no construtor pela {@link ContagemPrazoStrategy} e nunca recalculado
 * - mudanca de calendario nao move prazo ja lancado.
 */
public class Prazo {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String descricao;
    private final LocalDate intimacao;
    private final int quantidadeDias;
    private final RegimeContagem regime;
    private final LocalDate vencimento;
    private final boolean fatal;
    private final Advogado responsavel;
    private final Set<NivelAlerta> alertasEmitidos;
    private boolean cumprido;
    private LocalDate cumpridoEm;

    /** Construcao de um prazo novo: o vencimento e derivado da Strategy. */
    public Prazo(NumeroCnj numeroProcesso,
                 String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 boolean fatal,
                 Advogado responsavel,
                 ContagemPrazoStrategy contagem) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("prazo exige processo");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao do prazo e obrigatoria");
        }
        if (intimacao == null) {
            throw new IllegalArgumentException("data de intimacao e obrigatoria");
        }
        if (quantidadeDias <= 0) {
            throw new IllegalArgumentException("prazo deve ter ao menos 1 dia");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("prazo exige advogado responsavel");
        }
        this.id = null;
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao;
        this.intimacao = intimacao;
        this.quantidadeDias = quantidadeDias;
        this.regime = contagem.regime();
        this.vencimento = contagem.calcularVencimento(intimacao, quantidadeDias);
        this.fatal = fatal;
        this.responsavel = responsavel;
        this.alertasEmitidos = EnumSet.noneOf(NivelAlerta.class);
        this.cumprido = false;
    }

    /** Reconstituicao a partir da persistencia (nao recalcula vencimento). */
    public Prazo(Long id,
                 NumeroCnj numeroProcesso,
                 String descricao,
                 LocalDate intimacao,
                 int quantidadeDias,
                 RegimeContagem regime,
                 LocalDate vencimento,
                 boolean fatal,
                 Advogado responsavel,
                 Set<NivelAlerta> alertasEmitidos,
                 boolean cumprido,
                 LocalDate cumpridoEm) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.descricao = descricao;
        this.intimacao = intimacao;
        this.quantidadeDias = quantidadeDias;
        this.regime = regime;
        this.vencimento = vencimento;
        this.fatal = fatal;
        this.responsavel = responsavel;
        this.alertasEmitidos = alertasEmitidos == null || alertasEmitidos.isEmpty()
                ? EnumSet.noneOf(NivelAlerta.class)
                : EnumSet.copyOf(alertasEmitidos);
        this.cumprido = cumprido;
        this.cumpridoEm = cumpridoEm;
    }

    public int diasRestantes(LocalDate hoje, ContagemPrazoStrategy contagem) {
        return contagem.diasRestantes(hoje, vencimento);
    }

    public boolean venceu(LocalDate hoje) {
        return hoje.isAfter(vencimento);
    }

    public boolean estaEmAberto() {
        return !cumprido;
    }

    /**
     * Marca o nivel como ja alertado. Retorna {@code false} quando o nivel ja
     * havia sido emitido - e assim que o motor evita reenviar o mesmo alerta a
     * cada varredura do dia.
     */
    public boolean registrarAlerta(NivelAlerta nivel) {
        return alertasEmitidos.add(nivel);
    }

    public boolean jaAlertou(NivelAlerta nivel) {
        return alertasEmitidos.contains(nivel);
    }

    public void cumprir(LocalDate quando) {
        if (cumprido) {
            throw new IllegalStateException("prazo ja cumprido em " + cumpridoEm);
        }
        this.cumprido = true;
        this.cumpridoEm = quando;
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getIntimacao() {
        return intimacao;
    }

    public int getQuantidadeDias() {
        return quantidadeDias;
    }

    public RegimeContagem getRegime() {
        return regime;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public boolean isFatal() {
        return fatal;
    }

    public Advogado getResponsavel() {
        return responsavel;
    }

    public boolean isCumprido() {
        return cumprido;
    }

    public LocalDate getCumpridoEm() {
        return cumpridoEm;
    }

    public Set<NivelAlerta> getAlertasEmitidos() {
        return Collections.unmodifiableSet(alertasEmitidos);
    }
}
