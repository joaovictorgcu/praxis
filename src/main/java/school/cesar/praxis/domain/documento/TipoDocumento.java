package school.cesar.praxis.domain.documento;

/** Pecas que o gerador de documentos sabe produzir. */
public enum TipoDocumento {

    PETICAO_INICIAL("Petição inicial"),
    CONTESTACAO("Contestação"),
    PROCURACAO("Procuração ad judicia"),
    /** Peca sem gerador compilado: existe para os modelos cadastrados. */
    PECA_AVULSA("Peça avulsa");

    private final String rotulo;

    TipoDocumento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
