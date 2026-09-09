package school.cesar.praxis.domain.modelo;

import java.util.regex.Pattern;

/**
 * Value Object: identificador do modelo escolhido pelo escritorio, no estilo
 * {@code COBRANCA_ALUGUEL}. Imutavel e autovalidado - e o que a geracao de peca
 * informa para escolher o modelo.
 */
public final class CodigoModelo {

    private static final Pattern FORMATO = Pattern.compile("[A-Z][A-Z0-9_]{2,39}");

    private final String valor;

    public CodigoModelo(String valor) {
        String normalizado = valor == null ? null : valor.trim().toUpperCase();
        if (normalizado == null || !FORMATO.matcher(normalizado).matches()) {
            throw new IllegalArgumentException(
                    "codigo de modelo invalido: " + valor
                            + " (use letras, numeros e underscore, de 3 a 40 caracteres)");
        }
        this.valor = normalizado;
    }

    public static CodigoModelo de(String valor) {
        return new CodigoModelo(valor);
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CodigoModelo outro && outro.valor.equals(valor);
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
