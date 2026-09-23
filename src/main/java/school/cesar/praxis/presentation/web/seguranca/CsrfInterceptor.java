package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Token sincronizado contra CSRF nas acoes do painel. Todo formulario POST
 * leva {@code _csrf} (hidden, via fragmento) igual ao token da sessao; sem
 * isso a acao responde 403. GET so garante que a sessao tenha um token para
 * a tela renderizar.
 *
 * <p>{@code /login} fica de fora: nao ha sessao autenticada a proteger, e um
 * token pre-login exigiria cookie antes do primeiro GET. {@code /sair} entra,
 * para um site externo nao derrubar a sessao do usuario.
 */
public class CsrfInterceptor implements HandlerInterceptor {

    public static final String CHAVE_SESSAO = "csrfToken";
    public static final String PARAMETRO = "_csrf";
    public static final String CABECALHO = "X-CSRF-Token";

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler)
            throws Exception {
        if (metodoSeguro(requisicao)) {
            tokenDa(requisicao.getSession(true));
            return true;
        }
        if (!tokenValido(requisicao)) {
            resposta.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "token CSRF ausente ou inválido; recarregue a página e tente de novo");
            return false;
        }
        return true;
    }

    /** Metodo que nao muda estado e por isso dispensa token. */
    static boolean metodoSeguro(HttpServletRequest requisicao) {
        return METODOS_SEGUROS.contains(requisicao.getMethod().toUpperCase());
    }

    /**
     * Token recebido (parametro {@code _csrf} ou cabecalho {@code X-CSRF-Token})
     * confere com o da sessao. Comparacao em tempo constante.
     *
     * <p>O cabecalho existe porque requisicao multipart nem sempre traz o
     * parametro antes do arquivo ser lido.
     */
    static boolean tokenValido(HttpServletRequest requisicao) {
        HttpSession sessao = requisicao.getSession(false);
        String esperado = sessao == null ? null : (String) sessao.getAttribute(CHAVE_SESSAO);
        String recebido = requisicao.getParameter(PARAMETRO);
        if (recebido == null) {
            recebido = requisicao.getHeader(CABECALHO);
        }
        return esperado != null && recebido != null
                && MessageDigest.isEqual(esperado.getBytes(), recebido.getBytes());
    }

    /** Devolve o token da sessao, criando um se ainda nao houver. */
    public static String tokenDa(HttpSession sessao) {
        if (sessao == null) {
            return null;
        }
        synchronized (sessao) {
            String token = (String) sessao.getAttribute(CHAVE_SESSAO);
            if (token == null) {
                byte[] bytes = new byte[32];
                ALEATORIO.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                sessao.setAttribute(CHAVE_SESSAO, token);
            }
            return token;
        }
    }
}
