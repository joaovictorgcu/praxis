package school.cesar.praxis.domain.feriado;

/**
 * Value Object: o foro onde o prazo corre. Um feriado estadual vale para toda a
 * UF; um feriado comarcal so vale no municipio do foro.
 */
public record Jurisdicao(String uf, String comarca) {

    public Jurisdicao {
        if (uf == null || uf.isBlank()) {
            throw new IllegalArgumentException("uf do foro e obrigatoria");
        }
        if (comarca == null || comarca.isBlank()) {
            throw new IllegalArgumentException("comarca do foro e obrigatoria");
        }
        uf = uf.trim().toUpperCase();
        comarca = comarca.trim();
    }

    public static Jurisdicao de(String uf, String comarca) {
        return new Jurisdicao(uf, comarca);
    }

    @Override
    public String toString() {
        return comarca + "/" + uf;
    }
}
