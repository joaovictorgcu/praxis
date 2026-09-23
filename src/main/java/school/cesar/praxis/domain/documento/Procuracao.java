package school.cesar.praxis.domain.documento;

/**
 * Peca concreta: procuracao ad judicia et extra. Sobrescreve os hooks de
 * cabecalho, enderecamento e qualificacao - procuracao nao se enderaca ao juizo.
 */
public class Procuracao extends GeradorDocumento {

    @Override
    protected String cabecalho(DadosDocumento dados) {
        return "PROCURAÇÃO AD JUDICIA ET EXTRA";
    }

    @Override
    protected String enderecamento(DadosDocumento dados) {
        return "Processo de referência: " + dados.numeroProcesso()
                + " - Comarca de " + dados.comarca() + ".";
    }

    @Override
    protected String qualificacao(DadosDocumento dados) {
        return "OUTORGANTE: " + dados.cliente() + ", "
                + dados.campo("qualificacaoOutorgante", "qualificação a preencher") + ".";
    }

    @Override
    protected String corpo(DadosDocumento dados) {
        return "OUTORGADO: " + dados.advogado().nome() + ", inscrito na OAB sob o n. "
                + dados.advogado().oab() + ".\n\n"
                + "PODERES: os da cláusula ad judicia et extra, para o foro em geral, "
                + "podendo propor ações, contestar, recorrer, transigir, receber e dar quitação, "
                + "substabelecer com ou sem reserva de iguais poderes.";
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "PODERES ESPECIAIS: " + dados.campo("poderesEspeciais", "nenhum poder especial outorgado") + ".";
    }

    @Override
    protected String assinatura(DadosDocumento dados) {
        return dados.comarca() + ", data da assinatura.\n\n"
                + dados.cliente() + "\nOutorgante";
    }

    @Override
    public TipoDocumento tipo() {
        return TipoDocumento.PROCURACAO;
    }
}
