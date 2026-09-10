package school.cesar.praxis.application.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta ao criar/consultar uma audiência.
 * Retorna dados para a camada de apresentação.
 */
public class AudienciaResponse {

    private Long id;
    private String numeroProcesso;
    private String nomeParteAutora;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String sala;
    private String observacoes;
    private boolean ativa;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public AudienciaResponse() {}

    public AudienciaResponse(Long id, String numeroProcesso, String nomeParteAutora,
                            LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                            String sala, String observacoes, boolean ativa,
                            LocalDateTime criadaEm, LocalDateTime atualizadaEm) {
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.observacoes = observacoes;
        this.ativa = ativa;
        this.criadaEm = criadaEm;
        this.atualizadaEm = atualizadaEm;
    }

    // Getters e Setters

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

    public String getNomeParteAutora() {
        return nomeParteAutora;
    }

    public void setNomeParteAutora(String nomeParteAutora) {
        this.nomeParteAutora = nomeParteAutora;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
