package school.cesar.praxis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpSession;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.presentation.web.seguranca.CsrfInterceptor;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Apoio dos testes HTTP do painel: toda requisicao do MockMvc leva o parametro
 * {@code _csrf} de teste, e as sessoes montadas a mao carregam o mesmo token -
 * assim os testes exercitam a regra de negocio sem repetir o ritual do CSRF,
 * que tem teste proprio em {@code SegurancaHttpTest}.
 */
@TestConfiguration
public class ApoioDeTesteWeb {

    public static final String TOKEN_CSRF = "token-de-teste";

    @Bean
    MockMvcBuilderCustomizer csrfPadrao() {
        return builder -> builder.defaultRequest(get("/").param(CsrfInterceptor.PARAMETRO, TOKEN_CSRF));
    }

    /** Sessao autenticada com senha definitiva e token CSRF de teste. */
    public static MockHttpSession sessaoDe(Long id, String nome, String oab, Papel papel) {
        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(UsuarioLogado.CHAVE_SESSAO,
                new UsuarioLogado(id, nome, oab.toLowerCase() + "@praxis.adv.br", oab, papel));
        sessao.setAttribute(CsrfInterceptor.CHAVE_SESSAO, TOKEN_CSRF);
        return sessao;
    }
}
