package school.cesar.praxis.domain.processo;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/** Value Object: numero unico de processo no padrao CNJ (NNNNNNN-DD.AAAA.J.TR.OOOO). */
@Embeddable
public class NumeroCnj {

    private static final Pattern FORMATO =
            Pattern.compile("\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}");

    private String valor;

    protected NumeroCnj() {
    }

    public NumeroCnj(String valor) {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Numero CNJ invalido: " + valor);
        }
        this.valor = valor;
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NumeroCnj outro && outro.valor.equals(valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor;
    }
}
