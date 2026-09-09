package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes do contrato HTTP da anexacao. Cobrem o que os cenarios BDD nao
 * alcancam: o upload multipart e o 403 do Proxy saindo pela API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnexoHttpTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private ObjectMapper json;

    private void processo(String numero, boolean segredo, String oab) {
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Construtora Alfa Ltda.", "Recife", segredo,
                "Ana Souza", "ana@praxis.adv.br", oab));
    }

    @Test
    @DisplayName("upload multipart junta o arquivo e o download devolve os mesmos bytes")
    void uploadEDownload() throws Exception {
        String numero = "0003333-33.2026.8.17.0003";
        processo(numero, false, "PE12345");

        byte[] bytes = "%PDF-1.4 conteudo de teste".getBytes();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "procuracao.pdf", "application/pdf", bytes);

        String resposta = mvc.perform(multipart("/api/anexos")
                        .file(arquivo)
                        .param("numeroProcesso", numero)
                        .param("descricao", "Procuracao assinada")
                        .param("oab", "PE12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("procuracao.pdf"))
                .andExpect(jsonPath("$.tipo").value("PDF"))
                .andExpect(jsonPath("$.tamanhoBytes").value(bytes.length))
                .andExpect(jsonPath("$.segredoJustica").value(false))
                .andReturn().getResponse().getContentAsString();

        long id = json.readTree(resposta).get("id").asLong();

        mvc.perform(get("/api/anexos/" + id).param("oab", "PE12345"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("procuracao.pdf")))
                .andExpect(content().contentTypeCompatibleWith("application/pdf"))
                .andExpect(content().bytes(bytes));

        mvc.perform(get("/api/anexos").param("processo", numero))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("procuracao.pdf"));
    }

    @Test
    @DisplayName("anexo de processo sigiloso responde 403 para OAB nao habilitada")
    void anexoSigilosoBloqueia() throws Exception {
        String numero = "0004444-44.2026.8.17.0004";
        processo(numero, true, "PE54321");

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "laudo.pdf", "application/pdf", "laudo sigiloso".getBytes());

        String resposta = mvc.perform(multipart("/api/anexos")
                        .file(arquivo)
                        .param("numeroProcesso", numero)
                        .param("oab", "PE54321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segredoJustica").value(true))
                .andReturn().getResponse().getContentAsString();

        long id = json.readTree(resposta).get("id").asLong();

        mvc.perform(get("/api/anexos/" + id).param("oab", "PE54321"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/anexos/" + id).param("oab", "PE99999"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("segredo de justica")));
    }

    @Test
    @DisplayName("tipo nao aceito e recusado antes de virar anexo")
    void tipoNaoAceito() throws Exception {
        String numero = "0005555-55.2026.8.17.0005";
        processo(numero, false, "PE12345");

        MockMultipartFile executavel = new MockMultipartFile(
                "arquivo", "virus.exe", "application/x-msdownload", "MZ".getBytes());

        // Recusa de invariante e erro do cliente (400), nao falha do servidor.
        mvc.perform(multipart("/api/anexos")
                        .file(executavel)
                        .param("numeroProcesso", numero)
                        .param("oab", "PE12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", containsString("tipo de arquivo nao aceito")));

        mvc.perform(get("/api/anexos").param("processo", numero))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
