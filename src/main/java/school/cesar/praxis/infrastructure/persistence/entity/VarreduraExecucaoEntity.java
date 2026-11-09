package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.prazo.OrigemVarredura;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uma execucao da varredura de prazos ({@code varredura_execucao}). */
@Entity
@Table(name = "varredura_execucao", indexes = {
        @Index(name = "idx_varredura_iniciada", columnList = "iniciada_em")
})
public class VarreduraExecucaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemVarredura origem;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "concluida_em", nullable = false)
    private LocalDateTime concluidaEm;

    @Column(name = "prazos_avaliados", nullable = false)
    private int prazosAvaliados;

    @Column(name = "alertas_emitidos", nullable = false)
    private int alertasEmitidos;

    @Column(length = 150)
    private String solicitante;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrigemVarredura getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemVarredura origem) {
        this.origem = origem;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public LocalDateTime getIniciadaEm() {
        return iniciadaEm;
    }

    public void setIniciadaEm(LocalDateTime iniciadaEm) {
        this.iniciadaEm = iniciadaEm;
    }

    public LocalDateTime getConcluidaEm() {
        return concluidaEm;
    }

    public void setConcluidaEm(LocalDateTime concluidaEm) {
        this.concluidaEm = concluidaEm;
    }

    public int getPrazosAvaliados() {
        return prazosAvaliados;
    }

    public void setPrazosAvaliados(int prazosAvaliados) {
        this.prazosAvaliados = prazosAvaliados;
    }

    public int getAlertasEmitidos() {
        return alertasEmitidos;
    }

    public void setAlertasEmitidos(int alertasEmitidos) {
        this.alertasEmitidos = alertasEmitidos;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }
}
