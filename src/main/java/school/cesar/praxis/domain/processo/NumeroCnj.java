package school.cesar.praxis.domain.processo;

import java.util.regex.Pattern;

/**
 * Value Object: numero unico do processo no padrao CNJ
 * (NNNNNNN-DD.AAAA.J.TR.OOOO). Imutavel e autovalidado: nao existe processo
 * com numero mal formado no dominio.
 */
public final class NumeroCnj {

    private static final Pattern FORMATO =
            Pattern.compile("\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}");

    private final String valor;

    public NumeroCnj(String valor) {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Numero CNJ invalido: " + valor);
        }
        this.valor = valor;
    }

    public static NumeroCnj de(String valor) {
        return new NumeroCnj(valor);
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
