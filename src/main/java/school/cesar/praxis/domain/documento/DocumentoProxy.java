package school.cesar.praxis.domain.documento;

/**
 * <b>Proxy</b> de protecao: intercepta o acesso ao documento e verifica o
 * segredo de justica (art. 189 do CPC) antes de devolver o conteudo. O objeto
 * real (o adaptador de persistencia) so e consultado quando ha um solicitante,
 * e o conteudo so sai daqui se a OAB estiver habilitada nos autos.
 */
public class DocumentoProxy implements AcessoDocumento {

    private final AcessoDocumento real;
    private final String oabSolicitante;

    public DocumentoProxy(AcessoDocumento real, String oabSolicitante) {
        this.real = real;
        this.oabSolicitante = oabSolicitante;
    }

    @Override
    public DocumentoGerado carregar(Long documentoId) {
        if (oabSolicitante == null || oabSolicitante.isBlank()) {
            throw new AcessoNegadoException("solicitante nao identificado por OAB");
        }
        DocumentoGerado documento = real.carregar(documentoId);
        if (!documento.podeSerLidoPor(oabSolicitante)) {
            throw new AcessoNegadoException(
                    "documento " + documentoId + " esta em segredo de justica e a OAB "
                            + oabSolicitante + " nao esta habilitada nos autos");
        }
        return documento;
    }

    /** Falha de dominio: acesso barrado pelo segredo de justica. */
    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String mensagem) {
            super(mensagem);
        }
    }
}
