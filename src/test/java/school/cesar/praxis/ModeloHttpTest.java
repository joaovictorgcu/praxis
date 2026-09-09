package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes do contrato HTTP do cadastro de modelos.
 *
 * <p>Existem porque os cenarios BDD chamam os casos de uso diretamente: um campo
 * que falte no corpo JSON do controller passa despercebido por eles. Aqui o
 * caminho e o mesmo do navegador.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ModeloHttpTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private ObjectMapper json;

    @Test
    @DisplayName("POST /api/modelos cadastra e GET /api/modelos lista o modelo")
    void cadastraEListaModelo() throws Exception {
        mvc.perform(post("/api/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "HTTP_COBRANCA",
                                  "nome": "Modelo via HTTP",
                                  "tipo": "PETICAO_INICIAL",
                                  "corpo": "Divida de {{valorDivida}} do cliente {{cliente}}.",
                                  "pedidos": "DOS PEDIDOS a) o pagamento.",
                                  "enderecaAoJuizo": true
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("HTTP_COBRANCA"))
                .andExpect(jsonPath("$.rotuloTipo").value("Peticao inicial"))
                // "cliente" vem dos autos, entao so "valorDivida" e cobrado do usuario.
                .andExpect(jsonPath("$.camposEsperados", contains("valorDivida")));

        mvc.perform(get("/api/modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'HTTP_COBRANCA')]", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/documentos aceita codigoModelo e gera a peca pelo modelo")
    void geraDocumentoPeloModeloViaHttp() throws Exception {
        String processo = "0002222-22.2026.8.17.0002";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                processo, "Construtora Alfa Ltda.", "Recife", false,
                "Ana Souza", "ana@praxis.adv.br", "PE12345"));

        mvc.perform(post("/api/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "HTTP_ACORDO",
                                  "nome": "Acordo via HTTP",
                                  "tipo": "PECA_AVULSA",
                                  "titulo": "Instrumento particular de acordo",
                                  "corpo": "Acordo de {{valorAcordo}} com {{cliente}}.",
                                  "pedidos": "CLAUSULAS a) quitacao reciproca.",
                                  "enderecaAoJuizo": false
                                }"""))
                .andExpect(status().isOk());

        String resposta = mvc.perform(post("/api/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroProcesso": "%s",
                                  "tipo": "PECA_AVULSA",
                                  "codigoModelo": "HTTP_ACORDO",
                                  "oabSolicitante": "PE12345",
                                  "campos": { "valorAcordo": "R$ 12.500,00" }
                                }""".formatted(processo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PECA_AVULSA"))
                .andReturn().getResponse().getContentAsString();

        JsonNode documento = json.readTree(resposta);

        mvc.perform(get("/api/documentos/" + documento.get("id").asLong())
                        .param("oab", "PE12345"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acordo de R$ 12.500,00")))
                .andExpect(content().string(containsString("INSTRUMENTO PARTICULAR DE ACORDO")))
                .andExpect(content().string(not(containsString("EXCELENTISSIMO"))));
    }
}
