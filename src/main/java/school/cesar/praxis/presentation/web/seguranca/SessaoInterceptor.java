package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Guarda do painel: sem usuario na sessao, redireciona ao login guardando a
 * URL pedida para voltar depois; acao marcada com {@link SomenteChefe}
 * pedida por advogado responde 403.
 *
 * <p>Vale so para {@code /painel/**}. A API REST continua aberta e recebendo
 * a OAB na requisicao, como antes - e o contrato dos testes HTTP e dos
 * scripts de exemplo do README.
 */
public class SessaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler)
            throws Exception {
        UsuarioLogado usuario = UsuarioLogado.da(requisicao.getSession(false));
        if (usuario == null) {
            resposta.sendRedirect(requisicao.getContextPath() + "/login?proximo="
                    + URLEncoder.encode(destinoOriginal(requisicao), StandardCharsets.UTF_8));
            return false;
        }
        // Senha provisoria: so a tela de conta (trocar senha) e sair ficam liberadas.
        String caminho = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        if (usuario.senhaProvisoria() && !caminho.startsWith("/painel/conta")) {
            resposta.sendRedirect(requisicao.getContextPath() + "/painel/conta?provisoria");
            return false;
        }
        if (exigeChefe(handler) && !usuario.chefe()) {
            resposta.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "acao reservada ao chefe do escritorio");
            return false;
        }
        return true;
    }

    private static boolean exigeChefe(Object handler) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return false;
        }
        return metodo.hasMethodAnnotation(SomenteChefe.class)
                || metodo.getBeanType().isAnnotationPresent(SomenteChefe.class);
    }

    /** So GET volta para onde estava; POST interrompido volta ao painel. */
    private static String destinoOriginal(HttpServletRequest requisicao) {
        if (!"GET".equalsIgnoreCase(requisicao.getMethod())) {
            return "/painel";
        }
        String uri = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        return requisicao.getQueryString() == null ? uri : uri + "?" + requisicao.getQueryString();
    }
}
