package school.cesar.praxis.application.dto;

import java.time.LocalDateTime;

/**
 * DTO para criar/atualizar uma audiência.
 * Recebe dados da camada de apresentação.
 */
public class CriarAudienciaRequest {

    private String numeroProcesso;
    private String nomeParteAutora;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String sala;
    private String observacoes;

    public CriarAudienciaRequest() {}

    public CriarAudienciaRequest(String numeroProcesso, String nomeParteAutora,
                                  LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                                  String sala, String observacoes) {
        this.numeroProcesso = numeroProcesso;
        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.observacoes = observacoes;
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
}
