package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "solicitacao_acesso", indexes = @Index(name = "idx_solicitacao_processo", columnList = "numero_processo"))
public class SolicitacaoAcessoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 25)
    private String numeroProcesso;

    @Column(name = "oab_solicitante", nullable = false, length = 20)
    private String oabSolicitante;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @Column(name = "solicitado_em", nullable = false)
    private LocalDate solicitadoEm;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "decidido_em")
    private LocalDate decididoEm;

    @Column(name = "decidido_por_oab", length = 20)
    private String decididoPorOab;

    @Column(name = "motivo_negacao", length = 500)
    private String motivoNegacao;

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

    public String getOabSolicitante() {
        return oabSolicitante;
    }

    public void setOabSolicitante(String oabSolicitante) {
        this.oabSolicitante = oabSolicitante;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public LocalDate getSolicitadoEm() {
        return solicitadoEm;
    }

    public void setSolicitadoEm(LocalDate solicitadoEm) {
        this.solicitadoEm = solicitadoEm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDecididoEm() {
        return decididoEm;
    }

    public void setDecididoEm(LocalDate decididoEm) {
        this.decididoEm = decididoEm;
    }

    public String getDecididoPorOab() {
        return decididoPorOab;
    }

    public void setDecididoPorOab(String decididoPorOab) {
        this.decididoPorOab = decididoPorOab;
    }

    public String getMotivoNegacao() {
        return motivoNegacao;
    }

    public void setMotivoNegacao(String motivoNegacao) {
        this.motivoNegacao = motivoNegacao;
    }
}
