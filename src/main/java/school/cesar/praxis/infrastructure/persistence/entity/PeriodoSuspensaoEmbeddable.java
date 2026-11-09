package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/** Linha de {@code prazo_suspensao}: um periodo em que o prazo nao correu. */
@Embeddable
public class PeriodoSuspensaoEmbeddable {

    @Column(nullable = false)
    private LocalDate inicio;

    /** Ultimo dia suspenso; nulo enquanto a suspensao esta aberta. */
    @Column
    private LocalDate fim;

    @Column(nullable = false, length = 300)
    private String motivo;

    public PeriodoSuspensaoEmbeddable() {
    }

    public PeriodoSuspensaoEmbeddable(LocalDate inicio, LocalDate fim, String motivo) {
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
