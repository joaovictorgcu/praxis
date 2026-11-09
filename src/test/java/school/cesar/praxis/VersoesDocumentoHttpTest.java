package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.usuario.Papel;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contrato HTTP das versoes da peca: a ficha mostra versoes e formularios, as
 * acoes pela tela criam versoes, e a API respeita estado (409), sigilo (403)
 * e sessao (401). Banco proprio, como em {@link PrazoHttpTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:praxis-versoes-http;DB_CLOSE_DELAY=-1")
class VersoesDocumentoHttpTest {

    private static final String RAFAEL = "PE45678";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired
    private DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;

    private static MockHttpSession rafael() {
        return ApoioDeTesteWeb.sessaoDe(4L, "Rafael Torres", RAFAEL, Papel.ADVOGADO);
    }

    private static MockHttpSession ana() {
        return ApoioDeTesteWeb.sessaoDe(2L, "Ana Souza", "PE12345", Papel.ADVOGADO);
    }

    private DocumentoGerado peticao(String numero, boolean sigilo) {
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(numero, "Joana Ferreira Lima",
                "Recife", sigilo, "Rafael Torres", "rafael.torres@praxis.adv.br", RAFAEL));
        return gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(numero,
                TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "A linha telefônica foi cortada."), RAFAEL));
    }

    @Test
    @DisplayName("ficha da peca em rascunho mostra versoes, edicao, regeneracao e exclusao")
    void fichaDoRascunho() throws Exception {
        DocumentoGerado peca = peticao("0020001-01.2026.8.17.0001", false);

        mvc.perform(get("/painel/documentos/" + peca.getId()).session(rafael()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Versões do Texto")))
                .andExpect(content().string(containsString("geração")))
                .andExpect(content().string(containsString("Editar Texto")))
                .andExpect(content().string(containsString("Regenerar pelo Gerador Original")))
                .andExpect(content().string(containsString("campo_fatos")))
                .andExpect(content().string(containsString("Excluir Rascunho")));
    }

    @Test
    @DisplayName("editar pela tela cria a v2; a comparacao v1 -> v2 mostra a linha incluida")
    void editarECompararPelaTela() throws Exception {
        DocumentoGerado peca = peticao("0020002-02.2026.8.17.0001", false);
        String ficha = "/painel/documentos/" + peca.getId();

        mvc.perform(post(ficha + "/editar").session(rafael())
                        .param("conteudo", peca.getConteudo().replace("\n", "\r\n") + "\r\nRequer prioridade.")
                        .param("resumo", "Pede prioridade de tramitação"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ficha))
                .andExpect(flash().attribute("mensagem", containsString("nova versão")));

        mvc.perform(get(ficha).session(rafael()))
                .andExpect(content().string(containsString("Pede prioridade de tramitação")))
                .andExpect(content().string(containsString("edição")));

        mvc.perform(get(ficha + "/comparar").session(rafael()).param("de", "1").param("para", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comparação v1 → v2")))
                .andExpect(content().string(containsString("+1 linha(s) incluída(s)")))
                .andExpect(content().string(containsString("−0 linha(s) removida(s)")));

        mvc.perform(get(ficha + "/versoes/1").session(rafael()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Texto da Versão 1")));
    }

    @Test
    @DisplayName("API: editar, listar, ler versao, comparar e restaurar")
    void apiDeVersoes() throws Exception {
        DocumentoGerado peca = peticao("0020003-03.2026.8.17.0001", false);
        String base = "/api/documentos/" + peca.getId();

        mvc.perform(put(base + "/conteudo").session(rafael())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\": \"Texto reescrito.\", \"resumo\": \"Reescreve\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versaoAtual").value(2));

        mvc.perform(get(base + "/versoes").session(rafael()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].origem").value("EDICAO"))
                .andExpect(jsonPath("$[1].atual").value(true));

        mvc.perform(get(base + "/versoes/1").session(rafael()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origem").value("GERACAO"))
                .andExpect(jsonPath("$.conteudo").value(containsString("A linha telefônica foi cortada.")));

        mvc.perform(get(base + "/comparar").session(rafael()).param("de", "1").param("para", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidas").value(1));

        mvc.perform(post(base + "/versoes/1/restaurar").session(rafael()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versaoAtual").value(3));
    }

    @Test
    @DisplayName("API: peca em revisao nao muda de texto (409); peca sigilosa barra OAB nao habilitada (403)")
    void estadoESigilo() throws Exception {
        DocumentoGerado emRevisao = peticao("0020004-04.2026.8.17.0001", false);
        enviarParaRevisao.executar(new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(emRevisao.getId()));
        mvc.perform(put("/api/documentos/" + emRevisao.getId() + "/conteudo").session(rafael())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\": \"Texto novo.\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("EM_REVISAO")));

        DocumentoGerado sigilosa = peticao("0020005-05.2026.8.17.0001", true);
        mvc.perform(get("/api/documentos/" + sigilosa.getId() + "/versoes/1").session(ana()))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/documentos/" + sigilosa.getId() + "/conteudo").session(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\": \"Tentativa.\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API: versoes exigem sessao; rascunho excluido some (204, depois 404)")
    void sessaoEExclusao() throws Exception {
        DocumentoGerado peca = peticao("0020006-06.2026.8.17.0001", false);

        mvc.perform(get("/api/documentos/" + peca.getId() + "/versoes"))
                .andExpect(status().isUnauthorized());

        mvc.perform(delete("/api/documentos/" + peca.getId()).session(ana()))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/documentos/" + peca.getId()).session(rafael()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/documentos/" + peca.getId()).session(rafael()))
                .andExpect(status().isNotFound());
    }
}
