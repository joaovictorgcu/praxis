package school.cesar.praxis.domain.documento;

/**
 * Uma linha do resultado da comparacao. {@code numeroAntes} e nulo na linha
 * incluida; {@code numeroDepois}, na removida.
 */
public record LinhaComparada(Tipo tipo, Integer numeroAntes, Integer numeroDepois, String texto) {

    public enum Tipo {
        IGUAL(" "),
        INCLUIDA("+"),
        REMOVIDA("-");

        private final String sinal;

        Tipo(String sinal) {
            this.sinal = sinal;
        }

        public String sinal() {
            return sinal;
        }
    }

    public boolean alterada() {
        return tipo != Tipo.IGUAL;
    }
}
