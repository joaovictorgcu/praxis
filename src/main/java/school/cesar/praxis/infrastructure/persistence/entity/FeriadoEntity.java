package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.feriado.Abrangencia;

import java.time.LocalDate;

/** Mapeamento objeto-relacional do agregado Feriado. */
@Entity
@Table(name = "feriado", indexes = @Index(name = "idx_feriado_data", columnList = "data"))
public class FeriadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String descricao;

    /** Data de referencia da regra; na recorrencia anual so dia e mes valem. */
    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "repete_todo_ano", nullable = false)
    private boolean repeteTodoAno;

    @Enumerated(EnumType.STRING)
    @Column(name = "abrangencia_nivel", nullable = false, length = 20)
    private Abrangencia.Nivel abrangenciaNivel;

    /** Nulo quando o feriado e nacional. */
    @Column(name = "abrangencia_valor", length = 120)
    private String abrangenciaValor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public boolean isRepeteTodoAno() {
        return repeteTodoAno;
    }

    public void setRepeteTodoAno(boolean repeteTodoAno) {
        this.repeteTodoAno = repeteTodoAno;
    }

    public Abrangencia.Nivel getAbrangenciaNivel() {
        return abrangenciaNivel;
    }

    public void setAbrangenciaNivel(Abrangencia.Nivel abrangenciaNivel) {
        this.abrangenciaNivel = abrangenciaNivel;
    }

    public String getAbrangenciaValor() {
        return abrangenciaValor;
    }

    public void setAbrangenciaValor(String abrangenciaValor) {
        this.abrangenciaValor = abrangenciaValor;
    }
}
