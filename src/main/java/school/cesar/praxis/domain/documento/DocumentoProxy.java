package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

/**
 * Proxy de protecao do documento gerado. A regra do segredo de justica vive em
 * {@link ProxyDeAcesso}, compartilhada com o anexo de arquivo; aqui so se fixa
 * o tipo protegido e o substantivo que aparece na recusa.
 */
public class DocumentoProxy extends ProxyDeAcesso<DocumentoGerado> implements AcessoDocumento {

    public DocumentoProxy(AcessoRestrito<DocumentoGerado> real, String oabSolicitante) {
        super(real, oabSolicitante, "documento");
    }
}
