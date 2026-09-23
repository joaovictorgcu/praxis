package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Guarda das mutacoes da API REST. Leitura segue aberta - {@code GET},
 * {@code HEAD}, {@code OPTIONS} e {@code TRACE} passam sem sessao, que e o
 * contrato dos scripts de exemplo do README e dos testes de consulta. Todo
 * POST, PUT, PATCH ou DELETE exige a mesma sessao do painel, o mesmo token
 * CSRF e o mesmo {@link SomenteChefe} das telas.
 *
 * <p>A recusa sai em JSON e nunca como redirect para {@code /login}: quem
 * chama a API e script ou fetch, e um 302 devolvendo a tela de login seria
 * lido como sucesso.
 *
 * <p>Sem sessao e 401 (falta autenticar), nao 403: o chamador ainda pode
 * conseguir se fizer login. Os demais casos sao 403 porque ja se sabe quem e
 * e a resposta nao muda repetindo a requisicao.
 */
public class SessaoApiInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler)
            throws IOException {
        if (CsrfInterceptor.metodoSeguro(requisicao)) {
            return true;
        }
        UsuarioLogado usuario = UsuarioLogado.da(requisicao.getSession(false));
        if (usuario == null) {
            return recusar(resposta, HttpServletResponse.SC_UNAUTHORIZED,
                    "faça login no painel antes de chamar esta operação");
        }
        if (!CsrfInterceptor.tokenValido(requisicao)) {
            return recusar(resposta, HttpServletResponse.SC_FORBIDDEN,
                    "token CSRF ausente ou inválido");
        }
        if (usuario.senhaProvisoria()) {
            return recusar(resposta, HttpServletResponse.SC_FORBIDDEN,
                    "troque a senha provisória antes de usar a API");
        }
        if (SessaoInterceptor.exigeChefe(handler) && !usuario.chefe()) {
            return recusar(resposta, HttpServletResponse.SC_FORBIDDEN,
                    "ação reservada ao chefe do escritório");
        }
        return true;
    }

    /** Mesmo formato de erro do {@code TratadorDeErrosRest}: {@code {"erro": ...}}. */
    private static boolean recusar(HttpServletResponse resposta, int status, String mensagem)
            throws IOException {
        resposta.setStatus(status);
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
        return false;
    }
}
