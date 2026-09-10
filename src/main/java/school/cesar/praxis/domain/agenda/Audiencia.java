package school.cesar.praxis.domain.agenda;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio que representa uma audiência judicial.
 * Responsabilidades:
 * - Manter os dados da audiência
 * - Validar regras de negócio da audiência
 * - Detectar sobreposição de horários
 */
@Entity
@Table(name = "audiencias")
public class Audiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroProcesso;

    @Column(nullable = false)
    private String nomeParteAutora;

    @Column(nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(nullable = false)
    private LocalDateTime dataHoraFim;

    @Column(nullable = false)
    private String sala;

    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em")
    private LocalDateTime atualizadaEm;

    // Construtores
    public Audiencia() {
        this.criadaEm = LocalDateTime.now();
    }

    public Audiencia(String numeroProcesso, String nomeParteAutora, 
                     LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, String sala) {
        validarParametros(numeroProcesso, nomeParteAutora, dataHoraInicio, dataHoraFim, sala);
        validarHorarios(dataHoraInicio, dataHoraFim);

        this.numeroProcesso = numeroProcesso;
        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.criadaEm = LocalDateTime.now();
        this.ativa = true;
    }

    // Métodos de negócio
    
    /**
     * Verifica se esta audiência tem conflito de horário com outra.
     * Dois períodos se sobrepõem quando:
     * - O início de um é antes do fim do outro E
     * - O fim de um é depois do início do outro
     * E ambas estão na mesma sala
     */
    public boolean temConflitoCom(Audiencia outra) {
        if (!this.sala.equals(outra.sala)) {
            return false; // Sem conflito se estão em salas diferentes
        }

        return this.dataHoraInicio.isBefore(outra.dataHoraFim) &&
               this.dataHoraFim.isAfter(outra.dataHoraInicio);
    }

    /**
     * Verifica se o período da audiência está totalmente contido em outro período
     */
    public boolean estaContidoEm(LocalDateTime inicio, LocalDateTime fim) {
        return !this.dataHoraInicio.isBefore(inicio) && !this.dataHoraFim.isAfter(fim);
    }

    /**
     * Verifica se a audiência é a mesma (mesmo ID ou mesmo número de processo)
     */
    public boolean temMesmoProcesso(Audiencia outra) {
        return this.numeroProcesso.equals(outra.numeroProcesso);
    }

    /**
     * Desativa a audiência sem removê-la do banco
     */
    public void desativar() {
        this.ativa = false;
        this.atualizadaEm = LocalDateTime.now();
    }

    /**
     * Reativa a audiência
     */
    public void reativar() {
        this.ativa = true;
        this.atualizadaEm = LocalDateTime.now();
    }

    /**
     * Atualiza os dados da audiência
     */
    public void atualizar(String nomeParteAutora, LocalDateTime dataHoraInicio,
                          LocalDateTime dataHoraFim, String sala, String observacoes) {
        validarHorarios(dataHoraInicio, dataHoraFim);

        this.nomeParteAutora = nomeParteAutora;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.sala = sala;
        this.observacoes = observacoes;
        this.atualizadaEm = LocalDateTime.now();
    }

    // Métodos auxiliares de validação
    
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

    private void validarHorarios(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {
        if (dataHoraFim.isBefore(dataHoraInicio) || dataHoraFim.equals(dataHoraInicio)) {
            throw new HorarioInvalidoException(
                "A hora final da audiência deve ser posterior à hora inicial"
            );
        }
    }

    // Getters e Setters
    
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

    @Override
    public String toString() {
        return "Audiencia{" +
                "id=" + id +
                ", numeroProcesso='" + numeroProcesso + '\'' +
                ", nomeParteAutora='" + nomeParteAutora + '\'' +
                ", dataHoraInicio=" + dataHoraInicio +
                ", dataHoraFim=" + dataHoraFim +
                ", sala='" + sala + '\'' +
                '}';
    }
}
