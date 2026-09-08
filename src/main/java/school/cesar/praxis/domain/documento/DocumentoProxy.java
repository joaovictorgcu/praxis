package school.cesar.praxis.domain.documento;

import java.util.Set;

/**
 * Proxy de protecao: documento em segredo de justica so e carregado se o
 * solicitante estiver habilitado nos autos (art. 189 CPC). A validacao ocorre
 * antes de qualquer leitura do objeto real.
 */
public class DocumentoProxy implements AcessoDocumento {

    private final AcessoDocumento real;
    private final boolean segredoJustica;
    private final Set<String> oabsHabilitadas;
    private final String oabSolicitante;

    public DocumentoProxy(AcessoDocumento real,
                          boolean segredoJustica,
                          Set<String> oabsHabilitadas,
                          String oabSolicitante) {
        this.real = real;
        this.segredoJustica = segredoJustica;
        this.oabsHabilitadas = Set.copyOf(oabsHabilitadas);
        this.oabSolicitante = oabSolicitante;
    }

    @Override
    public String carregar(String documentoId) {
        if (segredoJustica && !oabsHabilitadas.contains(oabSolicitante)) {
            throw new AcessoNegadoException(
                    "documento em segredo de justica: OAB " + oabSolicitante + " nao habilitada nos autos");
        }
        return real.carregar(documentoId);
    }

    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String mensagem) {
            super(mensagem);
        }
    }
}
