package school.cesar.praxis.domain.documento;

public class PeticaoInicial extends GeradorDocumento {

    @Override
    protected String corpo(DadosDocumento dados) {
        return "DOS FATOS\n" + dados.campos().getOrDefault("fatos", "(fatos a preencher)")
                + "\n\nDO DIREITO\n" + dados.campos().getOrDefault("fundamentos", "(fundamentos a preencher)");
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "DOS PEDIDOS\na) a citacao da parte re;\nb) a procedencia integral dos pedidos;\n"
                + "c) a condenacao da re em custas e honorarios sucumbenciais.";
    }

    @Override
    public String tipo() {
        return "PETICAO_INICIAL";
    }
}
