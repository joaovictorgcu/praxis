package school.cesar.praxis.domain.documento;

/** Peca concreta: peticao inicial (CPC art. 319). */
public class PeticaoInicial extends GeradorDocumento {

    @Override
    protected String corpo(DadosDocumento dados) {
        return "DOS FATOS\n" + dados.campo("fatos", "(fatos a preencher)")
                + "\n\nDO DIREITO\n" + dados.campo("fundamentos", "(fundamentos a preencher)")
                + "\n\nDO VALOR DA CAUSA\nDa-se a causa o valor de "
                + dados.campo("valorCausa", "R$ 0,00") + ".";
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "DOS PEDIDOS\n"
                + "a) a citacao da parte re para, querendo, apresentar contestacao;\n"
                + "b) a procedencia integral dos pedidos, com "
                + dados.campo("pedidoPrincipal", "a condenacao da re na obrigacao discutida") + ";\n"
                + "c) a condenacao da re em custas processuais e honorarios sucumbenciais;\n"
                + "d) a producao de todas as provas em direito admitidas.";
    }

    @Override
    public TipoDocumento tipo() {
        return TipoDocumento.PETICAO_INICIAL;
    }
}
