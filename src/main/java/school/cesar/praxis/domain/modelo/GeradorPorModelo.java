package school.cesar.praxis.domain.modelo;

import school.cesar.praxis.domain.documento.DadosDocumento;
import school.cesar.praxis.domain.documento.GeradorDocumento;
import school.cesar.praxis.domain.documento.TipoDocumento;

/**
 * Subclasse do <b>Template Method</b> que le as secoes de um
 * {@link ModeloDocumento} cadastrado, em vez de traze-las compiladas.
 *
 * <p>E o que permite modelo cadastravel <i>sem</i> abrir mao do padrao: o
 * {@code gerar()} continua {@code final} na superclasse, a ordem das secoes
 * segue sendo regra do dominio, e o modelo preenche exatamente os dois passos
 * que sempre foram abstratos - corpo e pedidos. Peticao inicial, contestacao e
 * procuracao continuam compiladas, lado a lado com esta.
 */
public class GeradorPorModelo extends GeradorDocumento {

    private final ModeloDocumento modelo;

    public GeradorPorModelo(ModeloDocumento modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("gerador exige um modelo");
        }
        this.modelo = modelo;
    }

    @Override
    protected String cabecalho(DadosDocumento dados) {
        return modelo.getTitulo() == null
                ? super.cabecalho(dados)
                : modelo.getTitulo().toUpperCase() + " - PROCESSO N. " + dados.numeroProcesso();
    }

    /**
     * As tres secoes a seguir tratam do juizo, entao seguem juntas: peca que nao
     * se enderaca ao juizo tambem nao fala com Vossa Excelencia nem pede
     * deferimento. E a mesma escolha que a procuracao faz, so que decidida pelo
     * cadastro em vez de por codigo.
     */
    @Override
    protected String enderecamento(DadosDocumento dados) {
        return modelo.isEnderecaAoJuizo()
                ? super.enderecamento(dados)
                : "Processo de referencia: " + dados.numeroProcesso()
                        + " - Comarca de " + dados.comarca() + ".";
    }

    @Override
    protected String qualificacao(DadosDocumento dados) {
        return modelo.isEnderecaAoJuizo()
                ? super.qualificacao(dados)
                : dados.cliente() + ", qualificado nos autos do processo em referencia,"
                        + " firma o presente instrumento.";
    }

    @Override
    protected String assinatura(DadosDocumento dados) {
        return modelo.isEnderecaAoJuizo()
                ? super.assinatura(dados)
                : dados.comarca() + ", data da assinatura.\n\n"
                        + dados.cliente() + "\n"
                        + dados.advogado().nome() + " - OAB " + dados.advogado().oab();
    }

    @Override
    protected String corpo(DadosDocumento dados) {
        return modelo.getCorpo().interpretar(ContextoTexto.de(dados));
    }

    @Override
    protected String pedidos(DadosDocumento dados) {
        return modelo.getPedidos().interpretar(ContextoTexto.de(dados));
    }

    @Override
    public TipoDocumento tipo() {
        return modelo.getTipo();
    }

    public ModeloDocumento getModelo() {
        return modelo;
    }
}
