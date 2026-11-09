package school.cesar.praxis.domain.documento;

/** Como nasceu cada versao do texto da peca. */
public enum OrigemVersao {

    /** Versao 1: o texto que o gerador (compilado ou modelo) produziu. */
    GERACAO("geração"),
    /** Texto alterado a mao pelo advogado. */
    EDICAO("edição"),
    /** Texto refeito pelo mesmo gerador, com campos novos. */
    REGENERACAO("regeneração"),
    /** Copia de uma versao anterior, que volta a ser o texto atual. */
    RESTAURACAO("restauração");

    private final String rotulo;

    OrigemVersao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
