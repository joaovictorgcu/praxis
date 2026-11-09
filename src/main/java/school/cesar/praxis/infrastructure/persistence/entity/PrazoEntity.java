package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import school.cesar.praxis.domain.prazo.PrerrogativaPrazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapeamento objeto-relacional do agregado Prazo.
 *
 * <p>{@code situacao} e a fonte da verdade do ciclo de vida. {@code cumprido}
 * continua gravado (true so quando a situacao e CUMPRIDO) para que uma versao
 * anterior da aplicacao, num rollback, ainda leia a tabela corretamente.
 */
@Entity
@Table(name = "prazo", indexes = {
        @Index(name = "idx_prazo_processo", columnList = "numero_processo"),
        @Index(name = "idx_prazo_vencimento", columnList = "vencimento"),
        @Index(name = "idx_prazo_situacao", columnList = "situacao, vencimento")
})
public class PrazoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 30)
    private String numeroProcesso;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private LocalDate intimacao;

    @Column(name = "quantidade_dias", nullable = false)
    private int quantidadeDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegimeContagem regime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrerrogativaPrazo prerrogativa = PrerrogativaPrazo.NENHUMA;

    /** Vencimento calculado na abertura; nunca muda. */
    @Column(name = "vencimento_original", nullable = false)
    private LocalDate vencimentoOriginal;

    /** Vencimento vigente: o original ajustado por suspensao, retomada e dilacao. */
    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(name = "dias_acrescidos", nullable = false)
    private int diasAcrescidos;

    @Column(nullable = false)
    private boolean fatal;

    @Column(name = "responsavel_nome", nullable = false, length = 120)
    private String responsavelNome;

    @Column(name = "responsavel_email", nullable = false, length = 160)
    private String responsavelEmail;

    @Column(name = "responsavel_oab", nullable = false, length = 20)
    private String responsavelOab;

    /** Niveis de alerta ja emitidos, separados por virgula (idempotencia por marco). */
    @Column(name = "alertas_emitidos", length = 200)
    private String alertasEmitidos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoPrazo situacao = SituacaoPrazo.EM_ABERTO;

    @Column(nullable = false)
    private boolean cumprido;

    @Column(name = "cumprido_em")
    private LocalDate cumpridoEm;

    /**
     * Periodos em que o prazo nao correu. Colecao de valores: o ciclo de vida
     * dos periodos e o do prazo. Lida junto com o prazo (o motor precisa dela
     * para recontar) e em uma consulta so para a lista inteira.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "prazo_suspensao", joinColumns = @JoinColumn(name = "prazo_id"))
    @OrderBy("inicio")
    private List<PeriodoSuspensaoEmbeddable> suspensoes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getIntimacao() {
        return intimacao;
    }

    public void setIntimacao(LocalDate intimacao) {
        this.intimacao = intimacao;
    }

    public int getQuantidadeDias() {
        return quantidadeDias;
    }

    public void setQuantidadeDias(int quantidadeDias) {
        this.quantidadeDias = quantidadeDias;
    }

    public RegimeContagem getRegime() {
        return regime;
    }

    public void setRegime(RegimeContagem regime) {
        this.regime = regime;
    }

    public PrerrogativaPrazo getPrerrogativa() {
        return prerrogativa;
    }

    public void setPrerrogativa(PrerrogativaPrazo prerrogativa) {
        this.prerrogativa = prerrogativa;
    }

    public LocalDate getVencimentoOriginal() {
        return vencimentoOriginal;
    }

    public void setVencimentoOriginal(LocalDate vencimentoOriginal) {
        this.vencimentoOriginal = vencimentoOriginal;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public int getDiasAcrescidos() {
        return diasAcrescidos;
    }

    public void setDiasAcrescidos(int diasAcrescidos) {
        this.diasAcrescidos = diasAcrescidos;
    }

    public boolean isFatal() {
        return fatal;
    }

    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public void setResponsavelNome(String responsavelNome) {
        this.responsavelNome = responsavelNome;
    }

    public String getResponsavelEmail() {
        return responsavelEmail;
    }

    public void setResponsavelEmail(String responsavelEmail) {
        this.responsavelEmail = responsavelEmail;
    }

    public String getResponsavelOab() {
        return responsavelOab;
    }

    public void setResponsavelOab(String responsavelOab) {
        this.responsavelOab = responsavelOab;
    }

    public String getAlertasEmitidos() {
        return alertasEmitidos;
    }

    public void setAlertasEmitidos(String alertasEmitidos) {
        this.alertasEmitidos = alertasEmitidos;
    }

    public SituacaoPrazo getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPrazo situacao) {
        this.situacao = situacao;
    }

    public boolean isCumprido() {
        return cumprido;
    }

    public void setCumprido(boolean cumprido) {
        this.cumprido = cumprido;
    }

    public LocalDate getCumpridoEm() {
        return cumpridoEm;
    }

    public void setCumpridoEm(LocalDate cumpridoEm) {
        this.cumpridoEm = cumpridoEm;
    }

    public List<PeriodoSuspensaoEmbeddable> getSuspensoes() {
        return suspensoes;
    }

    public void setSuspensoes(List<PeriodoSuspensaoEmbeddable> suspensoes) {
        this.suspensoes = suspensoes;
    }
}
