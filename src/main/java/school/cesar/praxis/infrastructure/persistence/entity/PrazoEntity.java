package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.prazo.RegimeContagem;

import java.time.LocalDate;

/** Mapeamento objeto-relacional do agregado Prazo. */
@Entity
@Table(name = "prazo", indexes = {
        @Index(name = "idx_prazo_processo", columnList = "numero_processo"),
        @Index(name = "idx_prazo_vencimento", columnList = "vencimento")
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

    @Column(nullable = false)
    private LocalDate vencimento;

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

    @Column(nullable = false)
    private boolean cumprido;

    @Column(name = "cumprido_em")
    private LocalDate cumpridoEm;

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

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
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
}
