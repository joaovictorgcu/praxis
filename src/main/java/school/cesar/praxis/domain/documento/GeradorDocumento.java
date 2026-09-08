package school.cesar.praxis.domain.documento;

/**
 * <b>Template Method</b>: o esqueleto de uma peca processual e sempre o mesmo
 * (cabecalho, enderecamento, qualificacao, corpo, pedidos, assinatura); cada
 * tipo de peca preenche apenas o que lhe e proprio.
 *
 * <p>{@link #gerar(DadosDocumento)} e {@code final} de proposito: a ordem das
 * secoes e regra do dominio, nao escolha da subclasse.
 */
public abstract class GeradorDocumento {

    /** Template method. */
    public final String gerar(DadosDocumento dados) {
        StringBuilder peca = new StringBuilder();
        peca.append(cabecalho(dados)).append("\n\n");
        peca.append(enderecamento(dados)).append("\n\n");
        peca.append(qualificacao(dados)).append("\n\n");
        peca.append(corpo(dados)).append("\n\n");
        peca.append(pedidos(dados)).append("\n\n");
        peca.append(assinatura(dados));
        return peca.toString();
    }

    // Passos com implementacao padrao (hooks sobrescreviveis)

    protected String cabecalho(DadosDocumento dados) {
        return tipo().rotulo().toUpperCase() + " - PROCESSO N. " + dados.numeroProcesso();
    }

    protected String enderecamento(DadosDocumento dados) {
        return "EXCELENTISSIMO SENHOR DOUTOR JUIZ DE DIREITO DA COMARCA DE "
                + dados.comarca().toUpperCase() + ".";
    }

    protected String qualificacao(DadosDocumento dados) {
        return dados.cliente() + ", por seu advogado que ao final assina, vem "
                + "respeitosamente a presenca de Vossa Excelencia expor e requerer o que segue.";
    }

    protected String assinatura(DadosDocumento dados) {
        return "Termos em que pede deferimento.\n"
                + dados.comarca() + ", data do protocolo.\n\n"
                + dados.advogado().nome() + "\nOAB " + dados.advogado().oab();
    }

    // Passos obrigatorios por tipo de peca

    protected abstract String corpo(DadosDocumento dados);

    protected abstract String pedidos(DadosDocumento dados);

    public abstract TipoDocumento tipo();
}
