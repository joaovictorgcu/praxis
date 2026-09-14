package school.cesar.praxis.domain.usuario;

/**
 * Papel de quem entra no sistema. O escritorio tem dois: o advogado, que
 * conduz os autos, e o chefe (socio ou coordenador), que responde pelo
 * escritorio e por isso e quem aprova peca antes do protocolo e quem mexe no
 * cadastro que afeta todo mundo (feriados e modelos).
 */
public enum Papel {

    ADVOGADO("Advogado"),
    CHEFE("Chefe");

    private final String rotulo;

    Papel(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Aprovar, rejeitar e desfazer decisao sobre peca e prerrogativa do chefe. */
    public boolean podeAprovarPeca() {
        return this == CHEFE;
    }

    /** Remover feriado ou modelo altera o que vale para todo o escritorio. */
    public boolean podeAdministrarCadastros() {
        return this == CHEFE;
    }
}
