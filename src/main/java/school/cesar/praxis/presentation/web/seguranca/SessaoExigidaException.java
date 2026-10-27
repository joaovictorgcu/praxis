package school.cesar.praxis.presentation.web.seguranca;

/**
 * Download de peca/anexo sem sessao. Diferente do {@code AcessoNegadoException}
 * do Proxy (OAB identificada mas sem habilitacao): aqui ninguem foi
 * identificado ainda, entao o status e 401, nao 403 - quem chama ainda pode
 * conseguir o conteudo se fizer login.
 */
public class SessaoExigidaException extends RuntimeException {
    public SessaoExigidaException(String mensagem) {
        super(mensagem);
    }
}
