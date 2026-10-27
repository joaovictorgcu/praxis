package school.cesar.praxis.domain.compartilhado;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <b>Proxy</b> de protecao: intercepta a leitura e verifica o segredo de
 * justica (art. 189 do CPC) antes de devolver o conteudo. O objeto real (o
 * adaptador de persistencia) so e consultado quando ha um solicitante, e o
 * conteudo so sai daqui se a {@link PoliticaDeLeituraDosAutos} permitir.
 *
 * <p>A regra e escrita uma vez e vale para todo {@link ConteudoRestrito} -
 * peca gerada ou arquivo anexado. {@code carregar} e {@code final} porque a
 * checagem nao e opcional para quem especializa o Proxy. Toda tentativa -
 * permitida ou negada - vai para a auditoria antes do Proxy devolver ou
 * recusar.
 */
public class ProxyDeAcesso<T extends ConteudoRestrito> implements AcessoRestrito<T> {

    private final AcessoRestrito<T> real;
    private final Solicitante solicitante;
    /** Como o conteudo se chama na mensagem de erro e na auditoria: "documento", "arquivo". */
    private final String substantivo;
    private final VerificadorDeAcessoAoProcesso verificadorAcesso;
    private final RegistradorDeAuditoria auditoria;
    private final LocalDate hoje;

    protected ProxyDeAcesso(AcessoRestrito<T> real, Solicitante solicitante, String substantivo,
                            VerificadorDeAcessoAoProcesso verificadorAcesso,
                            RegistradorDeAuditoria auditoria, LocalDate hoje) {
        this.real = real;
        this.solicitante = solicitante;
        this.substantivo = substantivo;
        this.verificadorAcesso = verificadorAcesso;
        this.auditoria = auditoria;
        this.hoje = hoje;
    }

    @Override
    public final T carregar(Long id) {
        String oab = solicitante.oab();
        if (oab == null || oab.isBlank()) {
            throw new AcessoNegadoException("solicitante não identificado por OAB");
        }
        T conteudo = real.carregar(id);
        boolean pode = PoliticaDeLeituraDosAutos.podeLer(conteudo, oab, verificadorAcesso, hoje);
        String motivo = pode ? null
                : substantivo + " em segredo de justiça: OAB não habilitada nos autos e sem acesso ativo ao processo";
        auditoria.registrar(EventoDeAuditoria.leitura(substantivo, id, conteudo.getNumeroProcesso(), solicitante,
                LocalDateTime.now(), pode ? ResultadoAcesso.PERMITIDO : ResultadoAcesso.NEGADO, motivo));
        if (!pode) {
            throw new AcessoNegadoException(
                    substantivo + " " + id + " está em segredo de justiça e a OAB "
                            + oab + " não está habilitada nos autos");
        }
        return conteudo;
    }

    /** Falha de dominio: acesso barrado pelo segredo de justica. */
    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String mensagem) {
            super(mensagem);
        }
    }
}
