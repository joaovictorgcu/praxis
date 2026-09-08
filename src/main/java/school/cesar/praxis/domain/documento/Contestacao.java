package school.cesar.praxis.domain.documento;

public class Contestacao extends GeradorDocumento {

    @Override
    protected String corpo(DadosDocumento dados) {
        return "DAS PRELIMINARES\n" + dados.campos().getOrDefault("preliminares", "(preliminares)")
                + "\n\nDO MERITO\n" + dados.campos().getOrDefault("merito", "(merito)");
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "DOS PEDIDOS\na) o acolhimento das preliminares;\n"
                + "b) subsidiariamente, a improcedencia integral dos pedidos.";
    }

    @Override
    public String tipo() {
        return "CONTESTACAO";
    }
}
