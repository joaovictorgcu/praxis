package school.cesar.praxis.domain.feriado;

import java.time.LocalDate;

/**
 * Raiz de agregado do subdominio nuclear <b>Prazos &amp; Agenda</b>: um dia em
 * que nao corre prazo processual.
 *
 * <p>Combina duas decisoes independentes: <i>quando</i> o feriado incide
 * ({@link RegraRecorrencia}) e <i>onde</i> ele vale ({@link Abrangencia}).
 * Separar as duas evita a explosao de tipos de feriado.
 */
public class Feriado {

    private final Long id;
    private final String descricao;
    private final RegraRecorrencia recorrencia;
    private final Abrangencia abrangencia;

    public Feriado(Long id,
                   String descricao,
                   RegraRecorrencia recorrencia,
                   Abrangencia abrangencia) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("descrição do feriado é obrigatória");
        }
        if (recorrencia == null) {
            throw new IllegalArgumentException("regra de recorrência é obrigatória");
        }
        if (abrangencia == null) {
            throw new IllegalArgumentException("abrangência do feriado é obrigatória");
        }
        this.id = id;
        this.descricao = descricao.trim();
        this.recorrencia = recorrencia;
        this.abrangencia = abrangencia;
    }

    public Feriado(String descricao, RegraRecorrencia recorrencia, Abrangencia abrangencia) {
        this(null, descricao, recorrencia, abrangencia);
    }

    /**
     * Suspende o expediente nesta data, neste foro? Exige as duas condicoes:
     * a regra tem de incidir na data <b>e</b> a abrangencia tem de alcancar o foro.
     */
    public boolean suspendeExpediente(LocalDate data, Jurisdicao onde) {
        return abrangencia.alcanca(onde) && recorrencia.incideEm(data);
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public RegraRecorrencia getRecorrencia() {
        return recorrencia;
    }

    public Abrangencia getAbrangencia() {
        return abrangencia;
    }
}
