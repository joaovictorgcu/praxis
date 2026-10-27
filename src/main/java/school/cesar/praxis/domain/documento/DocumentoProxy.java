package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Solicitante;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;

import java.time.LocalDate;

/**
 * Proxy de protecao do documento gerado. A regra do segredo de justica vive em
 * {@link ProxyDeAcesso}, compartilhada com o anexo de arquivo; aqui so se fixa
 * o tipo protegido e o substantivo que aparece na recusa e na auditoria.
 */
public class DocumentoProxy extends ProxyDeAcesso<DocumentoGerado> implements AcessoDocumento {

    public DocumentoProxy(AcessoRestrito<DocumentoGerado> real, Solicitante solicitante,
                          VerificadorDeAcessoAoProcesso verificadorAcesso,
                          RegistradorDeAuditoria auditoria, LocalDate hoje) {
        super(real, solicitante, "documento", verificadorAcesso, auditoria, hoje);
    }
}
