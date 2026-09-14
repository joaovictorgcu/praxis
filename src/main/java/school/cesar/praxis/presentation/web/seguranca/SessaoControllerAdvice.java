package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expoe o usuario da sessao como {@code ${usuario}} em toda tela do painel,
 * para o cabecalho mostrar quem esta logado e as acoes do chefe aparecerem
 * so para o chefe.
 */
@ControllerAdvice(basePackages = "school.cesar.praxis.presentation.web")
public class SessaoControllerAdvice {

    @ModelAttribute("usuario")
    public UsuarioLogado usuario(HttpSession sessao) {
        return UsuarioLogado.da(sessao);
    }

    /** Token anti-CSRF para o hidden dos formularios (fragmento {@code csrf}). */
    @ModelAttribute("csrf")
    public String csrf(HttpSession sessao) {
        return CsrfInterceptor.tokenDa(sessao);
    }
}
