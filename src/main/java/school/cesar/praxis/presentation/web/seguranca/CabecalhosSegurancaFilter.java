package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Cabecalhos de seguranca que o Spring Security emitiria e que, sem ele,
 * ninguem emite. A CSP so permite script e estilo do proprio host: por isso
 * as telas nao tem {@code onclick} inline - o comportamento fica em
 * {@code /js/praxis.js} e em atributos {@code data-*}.
 */
@Component
public class CabecalhosSegurancaFilter extends OncePerRequestFilter {

    static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data:",
            "font-src 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'");

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        resposta.setHeader("X-Content-Type-Options", "nosniff");
        resposta.setHeader("X-Frame-Options", "DENY");
        resposta.setHeader("Referrer-Policy", "same-origin");
        resposta.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        if (!requisicao.getRequestURI().startsWith(requisicao.getContextPath() + "/h2-console")) {
            resposta.setHeader("Content-Security-Policy", CSP);
        }
        if (requisicao.getRequestURI().startsWith(requisicao.getContextPath() + "/painel")
                || requisicao.getRequestURI().startsWith(requisicao.getContextPath() + "/login")) {
            // Pagina autenticada nao entra em cache compartilhado nem fica no historico do proxy.
            resposta.setHeader("Cache-Control", "no-store");
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
