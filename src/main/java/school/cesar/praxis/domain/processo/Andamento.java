package school.cesar.praxis.domain.processo;

import java.time.LocalDate;

/**
 * Entidade do agregado Processo: um evento na linha do tempo processual.
 * Dominio puro - o mapeamento objeto-relacional vive na camada de infraestrutura.
 */
public class Andamento {

    private final Long id;
    private final LocalDate data;
    private final String descricao;
    private final TipoAndamento tipo;

    public Andamento(Long id, LocalDate data, String descricao, TipoAndamento tipo) {
        if (data == null) {
            throw new IllegalArgumentException("data do andamento e obrigatoria");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descricao do andamento e obrigatoria");
        }
        this.id = id;
        this.data = data;
        this.descricao = descricao;
        this.tipo = tipo == null ? TipoAndamento.OUTRO : tipo;
    }

    public Andamento(LocalDate data, String descricao, TipoAndamento tipo) {
        this(null, data, descricao, tipo);
    }

    /** Intimacao e citacao sao os andamentos que disparam contagem de prazo. */
    public boolean iniciaContagemDePrazo() {
        return tipo == TipoAndamento.INTIMACAO || tipo == TipoAndamento.CITACAO;
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
}
