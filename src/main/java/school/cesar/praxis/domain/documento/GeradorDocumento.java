package school.cesar.praxis.domain.documento;

/**
 * Template Method: o esqueleto de geracao de uma peca e sempre o mesmo
 * (cabecalho, enderecamento, qualificacao, corpo, pedidos, fechamento);
 * as subclasses preenchem apenas o que muda por tipo de peca.
 */
public abstract class GeradorDocumento {

    /** Template method - final para garantir a ordem das secoes. */
    public final String gerar(DadosDocumento dados) {
        StringBuilder sb = new StringBuilder();
        sb.append(cabecalho(dados)).append("\n\n");
        sb.append(enderecamento(dados)).append("\n\n");
        sb.append(qualificacao(dados)).append("\n\n");
        sb.append(corpo(dados)).append("\n\n");
        sb.append(pedidos(dados)).append("\n\n");
        sb.append(fechamento(dados));
        return sb.toString();
    }

    // Passos com implementacao padrao (hooks sobrescreviveis)
    protected String cabecalho(DadosDocumento dados) {
        return "PROCESSO N. " + dados.numeroProcesso();
    }

    protected String enderecamento(DadosDocumento dados) {
        return "EXCELENTISSIMO SENHOR DOUTOR JUIZ DE DIREITO DA COMARCA DE "
                + dados.comarca().toUpperCase() + ".";
    }

    protected String qualificacao(DadosDocumento dados) {
        return dados.cliente() + ", por seu advogado que ao final assina, vem respeitosamente "
                + "a presenca de Vossa Excelencia expor e requerer o que segue.";
    }

    protected String fechamento(DadosDocumento dados) {
        return "Termos em que pede deferimento.\n" + dados.comarca() + ", data do protocolo.";
    }

    // Passos obrigatorios por tipo de peca
    protected abstract String corpo(DadosDocumento dados);

    protected abstract String pedidos(DadosDocumento dados);

    public abstract String tipo();
}
