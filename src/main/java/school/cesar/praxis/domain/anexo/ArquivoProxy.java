package school.cesar.praxis.domain.anexo;

import school.cesar.praxis.domain.compartilhado.AcessoRestrito;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.compartilhado.RegistradorDeAuditoria;
import school.cesar.praxis.domain.compartilhado.Solicitante;
import school.cesar.praxis.domain.compartilhado.VerificadorDeAcessoAoProcesso;

import java.time.LocalDate;

/**
 * Proxy de protecao do anexo. Mesma regra do documento gerado, herdada de
 * {@link ProxyDeAcesso}: o arquivo em segredo de justica nao sai da
 * persistencia sem OAB habilitada nos autos ou acesso ativo ao processo.
 */
public class ArquivoProxy extends ProxyDeAcesso<ArquivoAnexo> implements AcessoArquivo {

    public ArquivoProxy(AcessoRestrito<ArquivoAnexo> real, Solicitante solicitante,
                        VerificadorDeAcessoAoProcesso verificadorAcesso,
                        RegistradorDeAuditoria auditoria, LocalDate hoje) {
        super(real, solicitante, "arquivo", verificadorAcesso, auditoria, hoje);
    }
}
