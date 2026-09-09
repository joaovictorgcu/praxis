package school.cesar.praxis.domain.anexo;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

/**
 * Proxy de protecao do anexo. Mesma regra do documento gerado, herdada de
 * {@link ProxyDeAcesso}: o arquivo em segredo de justica nao sai da
 * persistencia sem OAB habilitada nos autos.
 */
public class ArquivoProxy extends ProxyDeAcesso<ArquivoAnexo> implements AcessoArquivo {

    public ArquivoProxy(AcessoRestrito<ArquivoAnexo> real, String oabSolicitante) {
        super(real, oabSolicitante, "arquivo");
    }
}
