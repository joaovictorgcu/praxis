package school.cesar.praxis.domain.documento;

/** Peca concreta: contestacao (CPC art. 335 e 336). */
public class Contestacao extends GeradorDocumento {

    @Override
    protected String corpo(DadosDocumento dados) {
        return "DAS PRELIMINARES\n" + dados.campo("preliminares", "(preliminares a preencher)")
                + "\n\nDO MÉRITO\n" + dados.campo("merito", "(mérito a preencher)");
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "DOS PEDIDOS\n"
                + "a) o acolhimento das preliminares arguidas, com a extincao do feito;\n"
                + "b) subsidiariamente, a improcedencia integral dos pedidos;\n"
                + "c) a condenação da parte autora em custas e honorários sucumbenciais.";
    }

    @Override
    public TipoDocumento tipo() {
        return TipoDocumento.CONTESTACAO;
    }
}
