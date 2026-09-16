package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contrato HTTP do acesso ao painel: sem sessao vai para o login, login com a
 * senha inicial abre sessao, e acao marcada como do chefe recusa advogado com
 * 403 mesmo que ele monte a requisicao na mao. A API REST segue aberta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class LoginHttpTest {

    @Autowired
    private MockMvc mvc;

    private static MockHttpSession sessaoDe(String nome, String oab, Papel papel) {
        return ApoioDeTesteWeb.sessaoDe(1L, nome, oab, papel);
    }

    @Test
    @DisplayName("painel sem sessao redireciona ao login guardando o destino")
    void painelExigeLogin() throws Exception {
        mvc.perform(get("/painel/documentos?processo=x"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?proximo=*painel*documentos*"));
    }

    @Test
    @DisplayName("login com a senha inicial abre sessao e o painel mostra quem entrou")
    void loginAbreSessao() throws Exception {
        MvcResult resultado = mvc.perform(post("/login")
                        .param("email", "Carla.Mendes@praxis.adv.br")
                        .param("senha", "praxis123")
                        .param("proximo", "/painel/feriados"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painel/feriados"))
                .andReturn();

        MockHttpSession sessao = (MockHttpSession) resultado.getRequest().getSession(false);
        assertNotNull(sessao);
        UsuarioLogado logado = UsuarioLogado.da(sessao);
        assertEquals("PE00001", logado.oab());
        assertTrue(logado.chefe());

        mvc.perform(get("/painel").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carla Mendes")))
                .andExpect(content().string(containsString("Sair")));
    }

    @Test
    @DisplayName("admin entra digitando so o usuario; e chefe; nao aparece como provisorio")
    void adminLoginCurto() throws Exception {
        MvcResult resultado = mvc.perform(post("/login")
                        .param("email", "admin")
                        .param("senha", "123"))
                .andExpect(redirectedUrl("/painel"))
                .andReturn();
        UsuarioLogado admin = UsuarioLogado.da((MockHttpSession) resultado.getRequest().getSession(false));
        assertEquals("admin@praxis.adv.br", admin.email());
        assertEquals("ADMIN", admin.oab());
        assertTrue(admin.chefe());
        assertFalse(admin.senhaProvisoria());
    }

    @Test
    @DisplayName("senha errada volta ao formulario com a mesma mensagem de e-mail desconhecido")
    void senhaErrada() throws Exception {
        mvc.perform(post("/login")
                        .param("email", "ana.souza@praxis.adv.br")
                        .param("senha", "errada!"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("e-mail ou senha invalidos")));

        mvc.perform(post("/login")
                        .param("email", "ninguem@praxis.adv.br")
                        .param("senha", "praxis123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("e-mail ou senha invalidos")));
    }

    @Test
    @DisplayName("destino externo no login e ignorado: sempre volta ao painel")
    void naoRedirecionaParaFora() throws Exception {
        mvc.perform(post("/login")
                        .param("email", "bruno.carvalho@praxis.adv.br")
                        .param("senha", "praxis123")
                        .param("proximo", "https://malicioso.example/"))
                .andExpect(redirectedUrl("/painel"));
    }

    @Test
    @DisplayName("aprovar peca e acao do chefe: advogado recebe 403, chefe passa pela guarda")
    void aprovarSoChefe() throws Exception {
        mvc.perform(post("/painel/documentos/999/aprovar")
                        .param("comentario", "ok")
                        .session(sessaoDe("Ana", "PE12345", Papel.ADVOGADO)))
                .andExpect(status().isForbidden());

        // Chefe passa pela guarda; a peca nao existe, entao o dominio recusa e a
        // tela recebe a mensagem por flash - nunca 403 nem 500.
        mvc.perform(post("/painel/documentos/999/aprovar")
                        .param("comentario", "ok")
                        .session(sessaoDe("Carla", "PE00001", Papel.CHEFE)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painel/documentos/999"))
                .andExpect(flash().attribute("erro", containsString("nao encontrado")));
    }

    @Test
    @DisplayName("remover feriado e modelo sao acoes do chefe")
    void removerSoChefe() throws Exception {
        MockHttpSession advogado = sessaoDe("Ana", "PE12345", Papel.ADVOGADO);
        mvc.perform(post("/painel/feriados/1/remover").session(advogado))
                .andExpect(status().isForbidden());
        mvc.perform(post("/painel/modelos/1/remover").session(advogado))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("sair invalida a sessao e o painel volta a exigir login")
    void sair() throws Exception {
        MockHttpSession sessao = sessaoDe("Ana", "PE12345", Papel.ADVOGADO);
        mvc.perform(post("/sair").session(sessao))
                .andExpect(redirectedUrl("/login"));
        assertTrue(sessao.isInvalid());
    }

    @Test
    @DisplayName("todas as telas do painel renderizam para quem esta logado")
    void telasRenderizam() throws Exception {
        MockHttpSession chefe = sessaoDe("Carla", "PE00001", Papel.CHEFE);
        for (String tela : new String[]{"/painel", "/painel/documentos", "/painel/anexos",
                "/painel/modelos", "/painel/feriados"}) {
            mvc.perform(get(tela).session(chefe))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Carla")))
                    .andExpect(content().string(containsString("Chefe")));
        }
    }

    @Test
    @DisplayName("fluxo de aprovacao pela tela: advogado gera e envia, chefe aprova, historico aparece")
    void fluxoDeAprovacaoPelaTela() throws Exception {
        MockHttpSession ana = sessaoDe("Ana", "PE12345", Papel.ADVOGADO);
        MockHttpSession carla = sessaoDe("Carla", "PE00001", Papel.CHEFE);

        String pagina = mvc.perform(post("/painel/documentos")
                        .param("numeroProcesso", "0001234-56.2026.8.17.0001")
                        .param("tipo", "PETICAO_INICIAL")
                        .param("fatos", "inadimplemento")
                        .session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("gerada como rascunho")))
                .andReturn().getResponse().getContentAsString();

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("Peca #(\\d+) gerada").matcher(pagina);
        assertTrue(m.find(), "id da peca nao apareceu na tela");
        String id = m.group(1);

        mvc.perform(post("/painel/documentos/" + id + "/enviar-revisao").session(ana))
                .andExpect(redirectedUrl("/painel/documentos/" + id))
                .andExpect(flash().attribute("mensagem", containsString("revisao")));

        mvc.perform(get("/painel/documentos/" + id).session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aguardando revisao do chefe")));

        mvc.perform(post("/painel/documentos/" + id + "/aprovar").param("comentario", "de acordo").session(carla))
                .andExpect(flash().attribute("mensagem", containsString("aprovada")));

        mvc.perform(get("/painel/documentos/" + id).session(carla))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Protocolar")))
                .andExpect(content().string(containsString("por OAB PE00001")))
                .andExpect(content().string(containsString("de acordo")));
    }

    @Test
    @DisplayName("leitura da API REST continua aberta, sem sessao")
    void apiSegueAberta() throws Exception {
        mvc.perform(get("/api/feriados")).andExpect(status().isOk());
        mvc.perform(get("/api/documentos")).andExpect(status().isOk());
    }
}
