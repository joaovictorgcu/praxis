package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.presentation.web.seguranca.CsrfInterceptor;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CSRF, cabecalhos de seguranca e senha provisoria. Aqui o MockMvc e o puro
 * (sem o parametro _csrf padrao), para o ritual completo ser exercitado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaHttpTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuariosUseCases.CadastrarUsuario cadastrar;

    private static MockHttpSession sessaoSemToken() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(UsuarioLogado.CHAVE_SESSAO,
                new UsuarioLogado(1L, "Ana", "ana@praxis.adv.br", "PE12345", Papel.ADVOGADO));
        return s;
    }

    @Test
    @DisplayName("POST no painel sem token CSRF e recusado; com o token da sessao passa")
    void csrf() throws Exception {
        MockHttpSession sessao = sessaoSemToken();

        // Sem token na sessao nem no formulario: 403.
        mvc.perform(post("/painel/prazos/1/cumprir").session(sessao))
                .andExpect(status().isForbidden());

        // GET gera o token na sessao e a tela o embute no hidden _csrf.
        String tela = mvc.perform(get("/painel").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = (String) sessao.getAttribute(CsrfInterceptor.CHAVE_SESSAO);
        assertNotNull(token);
        assertTrue(tela.contains("name=\"_csrf\" value=\"" + token + "\""), "hidden _csrf ausente");

        // Token errado: 403. Token certo: a acao roda (prazo inexistente vira flash de erro).
        mvc.perform(post("/painel/prazos/1/cumprir").session(sessao).param("_csrf", "forjado"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/painel/prazos/999999/cumprir").session(sessao).param("_csrf", token))
                .andExpect(status().is3xxRedirection());

        // Sair tambem exige token: site externo nao derruba a sessao.
        mvc.perform(post("/sair").session(sessao)).andExpect(status().isForbidden());
        assertFalse(sessao.isInvalid());
        mvc.perform(post("/sair").session(sessao).header(CsrfInterceptor.CABECALHO, token))
                .andExpect(redirectedUrl("/login"));
        assertTrue(sessao.isInvalid());
    }

    @Test
    @DisplayName("cabecalhos de seguranca em toda resposta; paginas do painel sem cache")
    void cabecalhos() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Cache-Control", "no-store"))
                // Sem script inline: o comportamento vem do arquivo, servido com o
                // hash do conteudo na URL (/js/praxis-<hash>.js).
                .andExpect(content().string(matchesPattern(
                        "(?s).*<script src=\"/js/praxis-[0-9a-f]{32}\\.js\" defer></script>.*")));

        mvc.perform(get("/api/feriados"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("senha provisoria: login cai na troca de senha e o resto do painel redireciona ate trocar")
    void senhaProvisoria() throws Exception {
        Usuario novo = cadastrar.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Fabio Nunes", "fabio@praxis.adv.br", "PE66666", Papel.ADVOGADO, "provisoria1"));
        assertTrue(novo.isSenhaProvisoria());

        MvcResult login = mvc.perform(post("/login")
                        .param("email", "fabio@praxis.adv.br").param("senha", "provisoria1"))
                .andExpect(redirectedUrl("/painel"))
                .andReturn();
        MockHttpSession sessao = (MockHttpSession) login.getRequest().getSession(false);
        assertTrue(UsuarioLogado.da(sessao).senhaProvisoria());

        mvc.perform(get("/painel").session(sessao))
                .andExpect(redirectedUrl("/painel/conta?provisoria"));
        mvc.perform(get("/painel/processos").session(sessao))
                .andExpect(redirectedUrl("/painel/conta?provisoria"));
        mvc.perform(get("/painel/conta").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sua senha e provisoria")));

        String token = (String) sessao.getAttribute(CsrfInterceptor.CHAVE_SESSAO);
        mvc.perform(post("/painel/conta/senha").session(sessao).param("_csrf", token)
                        .param("senhaAtual", "provisoria1").param("novaSenha", "definitiva1")
                        .param("confirmacao", "definitiva1"))
                .andExpect(redirectedUrl("/login"));

        MvcResult segundoLogin = mvc.perform(post("/login")
                        .param("email", "fabio@praxis.adv.br").param("senha", "definitiva1"))
                .andExpect(redirectedUrl("/painel"))
                .andReturn();
        MockHttpSession nova = (MockHttpSession) segundoLogin.getRequest().getSession(false);
        assertFalse(UsuarioLogado.da(nova).senhaProvisoria());
        mvc.perform(get("/painel").session(nova)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("usuarios iniciais em dev entram sem troca obrigatoria")
    void iniciaisSemTroca() throws Exception {
        MvcResult login = mvc.perform(post("/login")
                        .param("email", "ana.souza@praxis.adv.br").param("senha", "praxis123"))
                .andReturn();
        MockHttpSession sessao = (MockHttpSession) login.getRequest().getSession(false);
        assertFalse(UsuarioLogado.da(sessao).senhaProvisoria());
        mvc.perform(get("/painel").session(sessao)).andExpect(status().isOk());
    }
}
