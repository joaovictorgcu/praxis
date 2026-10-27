package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "acesso_ao_processo", indexes = @Index(name = "idx_acesso_processo", columnList = "numero_processo"))
public class AcessoAoProcessoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_processo", nullable = false, length = 25)
    private String numeroProcesso;

    @Column(nullable = false, length = 20)
    private String oab;

    @Column(name = "concedido_por", nullable = false, length = 20)
    private String concedidoPor;

    @Column(name = "concedido_em", nullable = false)
    private LocalDate concedidoEm;

    @Column(name = "expira_em")
    private LocalDate expiraEm;

    @Column(name = "revogado_em")
    private LocalDate revogadoEm;

    @Column(name = "motivo_revogacao", length = 500)
    private String motivoRevogacao;

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

    public String getOab() {
        return oab;
    }

    public void setOab(String oab) {
        this.oab = oab;
    }

    public String getConcedidoPor() {
        return concedidoPor;
    }

    public void setConcedidoPor(String concedidoPor) {
        this.concedidoPor = concedidoPor;
    }

    public LocalDate getConcedidoEm() {
        return concedidoEm;
    }

    public void setConcedidoEm(LocalDate concedidoEm) {
        this.concedidoEm = concedidoEm;
    }

    public LocalDate getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDate expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDate getRevogadoEm() {
        return revogadoEm;
    }

    public void setRevogadoEm(LocalDate revogadoEm) {
        this.revogadoEm = revogadoEm;
    }

    public String getMotivoRevogacao() {
        return motivoRevogacao;
    }

    public void setMotivoRevogacao(String motivoRevogacao) {
        this.motivoRevogacao = motivoRevogacao;
    }
}
