package school.cesar.praxis.domain.processo;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Entidade do agregado Processo: um evento na linha do tempo processual. */
@Entity
@Table(name = "andamento")
public class Andamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAndamento tipo;

    protected Andamento() {
    }

    public Andamento(LocalDate data, String descricao, TipoAndamento tipo) {
        if (data == null) throw new IllegalArgumentException("data obrigatoria");
        if (descricao == null || descricao.isBlank()) throw new IllegalArgumentException("descricao obrigatoria");
        this.data = data;
        this.descricao = descricao;
        this.tipo = tipo == null ? TipoAndamento.OUTRO : tipo;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoAndamento getTipo() {
        return tipo;
    }

    /** Intimacao e citacao disparam contagem de prazo na linguagem do dominio. */
    public boolean iniciaContagemDePrazo() {
        return tipo == TipoAndamento.INTIMACAO || tipo == TipoAndamento.CITACAO;
    }
}
