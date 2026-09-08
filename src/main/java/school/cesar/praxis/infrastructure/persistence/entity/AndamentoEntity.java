package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.time.LocalDate;

/** Mapeamento objeto-relacional da entidade Andamento. */
@Entity
@Table(name = "andamento")
public class AndamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAndamento tipo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoAndamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAndamento tipo) {
        this.tipo = tipo;
    }
}
