package school.cesar.praxis.domain.feriado;

/**
 * Value Object: onde o feriado suspende o expediente.
 *
 * <p>Feriado nacional vale em todo o pais e por isso nao guarda valor de
 * referencia; estadual guarda a UF; comarcal guarda o nome da comarca.
 */
public record Abrangencia(Nivel nivel, String valor) {

    public enum Nivel {
        NACIONAL, ESTADUAL, COMARCAL
    }

    public Abrangencia {
        if (nivel == null) {
            throw new IllegalArgumentException("nivel de abrangencia e obrigatorio");
        }
        valor = valor == null ? null : valor.trim();
        if (nivel == Nivel.NACIONAL) {
            // Feriado nacional nao se qualifica por UF nem comarca.
            valor = null;
        } else if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "abrangencia " + nivel + " exige o valor de referencia");
        } else if (nivel == Nivel.ESTADUAL) {
            valor = valor.toUpperCase();
        }
    }

    public static Abrangencia nacional() {
        return new Abrangencia(Nivel.NACIONAL, null);
    }

    public static Abrangencia estadual(String uf) {
        return new Abrangencia(Nivel.ESTADUAL, uf);
    }

    public static Abrangencia comarcal(String comarca) {
        return new Abrangencia(Nivel.COMARCAL, comarca);
    }

    /** O feriado alcanca o foro informado? */
    public boolean alcanca(Jurisdicao onde) {
        return switch (nivel) {
            case NACIONAL -> true;
            case ESTADUAL -> valor.equalsIgnoreCase(onde.uf());
            case COMARCAL -> valor.equalsIgnoreCase(onde.comarca());
        };
    }

    public String rotulo() {
        return nivel == Nivel.NACIONAL ? "Nacional" : nivel + " " + valor;
    }
}
