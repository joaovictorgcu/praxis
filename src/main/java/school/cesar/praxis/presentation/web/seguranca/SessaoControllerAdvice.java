package school.cesar.praxis.presentation.web.seguranca;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import school.cesar.praxis.presentation.web.AssetUrl;

/**
 * Expoe o usuario da sessao como {@code ${usuario}} em toda tela do painel,
 * para o cabecalho mostrar quem esta logado e as acoes do chefe aparecerem
 * so para o chefe.
 */
@ControllerAdvice(basePackages = "school.cesar.praxis.presentation.web")
public class SessaoControllerAdvice {

    private final AssetUrl assetUrl;

    public SessaoControllerAdvice(AssetUrl assetUrl) {
        this.assetUrl = assetUrl;
    }

    @ModelAttribute("usuario")
    public UsuarioLogado usuario(HttpSession sessao) {
        return UsuarioLogado.da(sessao);
    }

    /** Token anti-CSRF para o hidden dos formularios (fragmento {@code csrf}). */
    @ModelAttribute("csrf")
    public String csrf(HttpSession sessao) {
        return CsrfInterceptor.tokenDa(sessao);
    }

    @ModelAttribute("assetCss")
    public String assetCss() {
        return assetUrl.css();
    }

    @ModelAttribute("assetJs")
    public String assetJs() {
        return assetUrl.js();
    }
}
