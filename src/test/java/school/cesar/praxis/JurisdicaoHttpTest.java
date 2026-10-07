package school.cesar.praxis;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.ComarcasUseCases;
import school.cesar.praxis.application.port.in.VarasUseCases;
import school.cesar.praxis.domain.usuario.Papel;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class JurisdicaoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ComarcasUseCases.Cadastrar comarcas;

    @Autowired
    private ComarcasUseCases.BuscarPorId buscarComarca;

    @Autowired
    private VarasUseCases.Cadastrar varas;

    @Autowired
    private VarasUseCases.BuscarPorId buscarVara;

    private MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Administrador", "ADMIN", Papel.CHEFE);
    }

    private ComarcasUseCases.ItemComarca comarca(String nome) {
        return comarcas.executar(
            new ComarcasUseCases.Cadastrar.Comando(
                nome,
                "Recife",
                "PE",
                "TJPE",
                "Rua do teste, 10",
                "8133334444",
                "foro@teste.jus.br",
                "9h às 17h",
                "Observação do fórum"
            )
        );
    }

    @Test
    void cadastraComarcaEExibeTodosOsDadosNaConsulta() throws Exception {
        mvc.perform(
            post("/api/comarcas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Comarca cadastro HTTP","municipio":"Olinda","uf":"pe","tribunal":"TJPE",
                     "endereco":"Rua do Fórum, 15","telefone":"8133330000","email":"foro@exemplo.jus.br",
                     "horarioAtendimento":"9h às 17h","observacoes":"Atendimento mediante agendamento"}
                    """
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uf").value("PE"))
            .andExpect(jsonPath("$.endereco").value("Rua do Fórum, 15"))
            .andExpect(jsonPath("$.dataCadastro").isNotEmpty());
        var c = comarca("Comarca ficha HTTP");
        mvc.perform(get("/api/comarcas/" + c.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value(c.nome()))
            .andExpect(jsonPath("$.email").value(c.email()))
            .andExpect(jsonPath("$.telefone").value(c.telefone()))
            .andExpect(jsonPath("$.municipio").value(c.municipio()))
            .andExpect(jsonPath("$.horarioAtendimento").value(c.horarioAtendimento()))
            .andExpect(jsonPath("$.observacoes").value(c.observacoes()));
        mvc.perform(get("/api/comarcas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void atualizaComarcaMantendoVinculoDasVarasEDataCadastro() throws Exception {
        var c = comarca("Comarca alteração HTTP");
        var v = varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                c.id(),
                "Vara vinculada",
                "Cível",
                null,
                null,
                null,
                null,
                null
            )
        );
        mvc.perform(
            put("/api/comarcas/" + c.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Comarca atualizada HTTP","municipio":"Recife","uf":"PE","tribunal":"TJPE",
                     "email":"novo@teste.jus.br","observacoes":"Dados revisados"}
                    """
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Comarca atualizada HTTP"))
            .andExpect(jsonPath("$.email").value("novo@teste.jus.br"));
        assertEquals(c.dataCadastro(), buscarComarca.executar(c.id()).dataCadastro());
        assertEquals(c.id(), buscarVara.executar(v.id()).comarcaId());
    }

    @Test
    void cadastraAtualizaEConsultaVaraInclusiveMudancaDeComarca() throws Exception {
        var c = comarca("Comarca vara origem HTTP");
        var destino = comarca("Comarca vara destino HTTP");
        mvc.perform(
            post("/api/varas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"comarcaId\":" +
                        c.id() +
                        ",\"nome\":\"Vara criada por HTTP\",\"competencia\":\"Família\"}"
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comarcaId").value(c.id()))
            .andExpect(jsonPath("$.competencia").value("Família"));
        var v = varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                c.id(),
                "Vara alteração",
                "Criminal",
                null,
                null,
                null,
                null,
                null
            )
        );
        mvc.perform(
            put("/api/varas/" + v.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"comarcaId\":" +
                        destino.id() +
                        ",\"nome\":\"Vara transferida\",\"competencia\":\"Cível\",\"telefone\":\"8133331111\",\"observacoes\":\"Sala 2\"}"
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comarcaId").value(destino.id()));
        mvc.perform(get("/api/varas/" + v.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Vara transferida"))
            .andExpect(jsonPath("$.telefone").value("8133331111"))
            .andExpect(jsonPath("$.observacoes").value("Sala 2"));
        assertEquals(v.dataCadastro(), buscarVara.executar(v.id()).dataCadastro());
        mvc.perform(get("/api/varas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void rejeitaDadosInvalidosDuplicadosEVinculosInexistentes() throws Exception {
        var c = comarca("Comarca duplicada HTTP");
        mvc.perform(
            post("/api/comarcas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"comarca duplicada http\",\"municipio\":\"recife\",\"uf\":\"PE\",\"tribunal\":\"TJPE\"}"
                )
        ).andExpect(status().isBadRequest());
        mvc.perform(
            post("/api/comarcas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"Inválida\",\"municipio\":\"Recife\",\"uf\":\"XX\",\"tribunal\":\"TJPE\"}"
                )
        ).andExpect(status().isBadRequest());
        mvc.perform(
            post("/api/varas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"comarcaId\":999999,\"nome\":\"Vara inexistente\",\"competencia\":\"Cível\"}")
        ).andExpect(status().isBadRequest());
        varas.executar(
            new VarasUseCases.Cadastrar.Comando(
                c.id(),
                "Vara duplicada",
                "Cível",
                null,
                null,
                null,
                null,
                null
            )
        );
        mvc.perform(
            post("/api/varas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"comarcaId\":" + c.id() + ",\"nome\":\"vara duplicada\",\"competencia\":\"Cível\"}"
                )
        ).andExpect(status().isBadRequest());
        mvc.perform(
            put("/api/comarcas/" + c.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"Inválida\",\"municipio\":\"Recife\",\"uf\":\"PE\",\"tribunal\":\"TJPE\",\"email\":\"invalido\"}"
                )
        ).andExpect(status().isBadRequest());
        assertEquals(c.nome(), buscarComarca.executar(c.id()).nome());
        mvc.perform(get("/api/varas/999999")).andExpect(status().isNotFound());
    }

    @Test
    void protegeMutacoesERenderizaTelaComAbasEModais() throws Exception {
        var advogado = ApoioDeTesteWeb.sessaoDe(2L, "Advogado", "PE12345", Papel.ADVOGADO);
        mvc.perform(post("/api/comarcas").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(
            status().isUnauthorized()
        );
        mvc.perform(
            post("/api/varas").session(advogado).contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isForbidden());
        mvc.perform(
            put("/api/comarcas/1").session(advogado).contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isForbidden());
        mvc.perform(get("/painel/comarcas").session(chefe()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"foro-aba-varas\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"foro-modal\"")));
    }
}
