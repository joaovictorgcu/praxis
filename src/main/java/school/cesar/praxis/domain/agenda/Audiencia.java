package school.cesar.praxis.domain.agenda;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Audiencia judicial. Java puro: persistencia vive em AudienciaEntity.
 */
public class Audiencia {

    private Long id;
    private String numeroProcesso;
    private String nomeParteAutora;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String sala;
    private String observacoes;
    private boolean ativa = true;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    public Audiencia(String numeroProcesso, String nomeParteAutora,
                     LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String sala) {
        validarParametros(numeroProcesso, nomeParteAutora, dataHoraInicio, dataHoraFim, sala);
        this.numeroProcesso = numeroProcesso;
        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.criadaEm = LocalDateTime.now();
        this.ativa = true;
    }

    /**
     * Reidrata audiencia ja persistida. Nao valida horario nem campos
     * obrigatorios: o banco e a fonte, e detectarConflitos pode vir com
     * numeroProcesso/nomeParteAutora nulos.
     */
    public Audiencia(Long id, String numeroProcesso, String nomeParteAutora,
                     LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String sala,
                     String observacoes, boolean ativa, LocalDateTime criadaEm,
                     LocalDateTime atualizadaEm) {
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

    public boolean temConflitoCom(Audiencia outra) {
        if (this.sala == null || outra.sala == null || !this.sala.equals(outra.sala)) {
            return false;
        }
        return this.dataHoraInicio.isBefore(outra.dataHoraFim) &&
               this.dataHoraFim.isAfter(outra.dataHoraInicio);
    }

    public boolean estaContidoEm(LocalDateTime inicio, LocalDateTime fim) {
        return !this.dataHoraInicio.isBefore(inicio) && !this.dataHoraFim.isAfter(fim);
    }

    public boolean temMesmoProcesso(Audiencia outra) {
        return this.numeroProcesso != null && this.numeroProcesso.equals(outra.numeroProcesso);
    }

    public void desativar() {
        this.ativa = false;
        this.atualizadaEm = LocalDateTime.now();
    }

    public void reativar() {
        this.ativa = true;
        this.atualizadaEm = LocalDateTime.now();
    }

    public void atualizar(String nomeParteAutora, LocalDateTime dataHoraInicio,
                          LocalDateTime dataHoraFim, String sala, String observacoes) {
        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.observacoes = observacoes;
        this.atualizadaEm = LocalDateTime.now();
    }

    private void validarParametros(String numeroProcesso, String nomeParteAutora,
                                    LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String sala) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            throw new IllegalArgumentException("Número do processo não pode ser nulo ou vazio");
        }
        if (nomeParteAutora == null || nomeParteAutora.isBlank()) {
            throw new IllegalArgumentException("Nome da parte autora não pode ser nulo ou vazio");
        }
        if (dataHoraInicio == null) {
            throw new IllegalArgumentException("Data e hora de início não podem ser nulas");
        }
        if (dataHoraFim == null) {
            throw new IllegalArgumentException("Data e hora de fim não podem ser nulas");
        }
        if (sala == null || sala.isBlank()) {
            throw new IllegalArgumentException("Sala não pode ser nula ou vazia");
        }
    }

    public Long getId() {
        return id;
    }

    public void atribuirId(Long id) {
        this.id = id;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getNomeParteAutora() {
        return nomeParteAutora;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public String getSala() {
        return sala;
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

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audiencia audiencia = (Audiencia) o;
        return Objects.equals(id, audiencia.id) &&
               Objects.equals(numeroProcesso, audiencia.numeroProcesso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numeroProcesso);
    }
}
