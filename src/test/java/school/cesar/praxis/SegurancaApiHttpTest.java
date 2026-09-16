package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.presentation.web.seguranca.CsrfInterceptor;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Guarda das mutacoes da API REST: o que o {@code SessaoApiInterceptor} recusa
 * e o que ele deixa passar.
 *
 * <p>Estes casos nao cabem no {@code SegurancaHttpTest}, que exercita o ritual
 * do painel - la a recusa e uma tela, aqui e JSON.
 *
 * <p>Sem o {@code ApoioDeTesteWeb} de proposito: o token so entra onde o teste
 * o coloca, entao "sem token nenhum" e mesmo sem token.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaApiHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired private DocumentosUseCases.GerarDocumento gerarDocumento;

    private static MockHttpSession advogada(String oab) {
        return ApoioDeTesteWeb.sessaoDe(2L, "Ana Souza", oab, Papel.ADVOGADO);
    }

    private static MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Carla Mendes", "PE00001", Papel.CHEFE);
    }

    @Test
    @DisplayName("mutacao sem login responde 401 em JSON; a leitura da mesma rota segue aberta")
    void mutacaoSemLogin() throws Exception {
        mvc.perform(post("/api/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "SEM_SESSAO",
                                  "nome": "Nao deveria entrar",
                                  "tipo": "PECA_AVULSA",
                                  "corpo": "corpo",
                                  "pedidos": "pedidos",
                                  "enderecaAoJuizo": false
                                }"""))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro", containsString("login")));

        // 401, e nao um 302 para a tela de login, que um script leria como sucesso.
        mvc.perform(post("/api/feriados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao":"Sem sessao","data":"2026-11-20",
                                 "repeteTodoAno":false,"nivel":"NACIONAL"}"""))
                .andExpect(status().isUnauthorized());

        mvc.perform(delete("/api/feriados/1")).andExpect(status().isUnauthorized());

        // O cadastro recusado nao chegou ao dominio.
        mvc.perform(get("/api/modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'SEM_SESSAO')]").isEmpty());
    }

    @Test
    @DisplayName("acao de chefe pedida por advogado responde 403, mesmo com sessao e token validos")
    void acaoDeChefePorAdvogado() throws Exception {
        String numero = "0001111-11.2026.8.17.0011";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Epsilon", "Recife", false,
                "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        DocumentoGerado peca = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "x"), "PE12345"));

        MockHttpSession ana = advogada("PE12345");

        mvc.perform(post("/api/documentos/" + peca.getId() + "/aprovar").session(ana)
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"de acordo\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro", containsString("chefe")));

        mvc.perform(post("/api/documentos/" + peca.getId() + "/oabs").session(ana)
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oab\":\"PE99999\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/modelos/1").session(ana)
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF))
                .andExpect(status().isForbidden());

        // O mesmo pedido, agora pelo chefe, passa da guarda: o 409 vem do dominio
        // (peca ainda em rascunho), nao da sessao.
        mvc.perform(post("/api/documentos/" + peca.getId() + "/aprovar").session(chefe())
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"de acordo\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST com sessao mas sem o token CSRF responde 403; com o token da sessao passa")
    void mutacaoSemCsrf() throws Exception {
        MockHttpSession carla = chefe();
        String corpo = """
                {"descricao":"Aniversario do Recife","data":"2026-10-15",
                 "repeteTodoAno":true,"nivel":"COMARCAL","abrangencia":"Recife"}""";

        // Sem token nenhum.
        mvc.perform(post("/api/feriados").session(carla)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro", containsString("CSRF")));

        // Token que nao e o da sessao.
        mvc.perform(post("/api/feriados").session(carla)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo)
                        .param(CsrfInterceptor.PARAMETRO, "forjado"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/feriados").session(carla)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo)
                        .header(CsrfInterceptor.CABECALHO, ApoioDeTesteWeb.TOKEN_CSRF))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("peca sigilosa: advogado logado nao le os autos com a OAB de outro")
    void sigilosoComOabDeOutro() throws Exception {
        String numero = "0002222-22.2026.8.17.0022";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Zeta", "Recife", true,
                "Bruno Carvalho", "bruno@praxis.adv.br", "PE54321"));
        DocumentoGerado peca = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "sigiloso"), "PE54321"));

        // Ana esta logada, mas a OAB dela nao esta habilitada nos autos.
        mvc.perform(get("/api/documentos/" + peca.getId()).session(advogada("PE12345"))
                        .param("oab", "PE12345"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("segredo de justica")));

        // Nem informando a OAB de quem esta habilitado: o Proxy barra a inscricao,
        // e a peca gerada por Ana passaria a sair assinada por Bruno.
        mvc.perform(get("/api/documentos/" + peca.getId()).session(advogada("PE12345"))
                        .param("oab", "PE99999"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/documentos/" + peca.getId()).session(advogada("PE54321"))
                        .param("oab", "PE54321"))
                .andExpect(status().isOk());
    }
}
