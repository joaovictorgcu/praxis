package school.cesar.praxis.domain.documento;

public class Procuracao extends GeradorDocumento {

    /** Procuracao nao se enderaca ao juizo: hook sobrescrito. */
    @Override
    protected String enderecamento(DadosDocumento dados) {
        return "PROCURACAO AD JUDICIA ET EXTRA";
    }

    @Override
    protected String corpo(DadosDocumento dados) {
        return "OUTORGANTE: " + dados.cliente()
                + "\nOUTORGADO: " + dados.campos().getOrDefault("advogado", "(advogado)")
                + "\nPODERES: os da clausula ad judicia et extra, para o foro em geral.";
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return "Poderes especiais: " + dados.campos().getOrDefault("poderesEspeciais", "nenhum");
    }

    @Override
    public String tipo() {
        return "PROCURACAO";
    }
}
