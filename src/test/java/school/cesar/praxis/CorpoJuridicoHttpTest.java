package school.cesar.praxis;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.application.port.out.EquipeRepositorio;
import school.cesar.praxis.domain.usuario.Papel;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class CorpoJuridicoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdvogadosUseCases.CadastrarAdvogado cadastrarAdvogado;

    @Autowired
    private AdvogadosUseCases.BuscarAdvogadoPorId buscarAdvogado;

    @Autowired
    private EquipesUseCases.CadastrarEquipe cadastrarEquipe;

    @Autowired
    private EquipeRepositorio equipes;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Admin", "ADMIN", Papel.CHEFE);
    }

    @Test
    void editaEquipeEExcluiVinculosPreservandoAdvogados() throws Exception {
        var a = cadastrarAdvogado.executar(
            new AdvogadosUseCases.CadastrarAdvogado.Comando(
                "Advogada equipe",
                "equipe.teste@praxis.adv.br",
                "PE987001",
                "81999990000",
                "Cível",
                true
            )
        );
        var b = cadastrarAdvogado.executar(
            new AdvogadosUseCases.CadastrarAdvogado.Comando(
                "Advogado equipe",
                "equipe.outro@praxis.adv.br",
                "PE987002",
                null,
                "Trabalhista",
                true
            )
        );
        var equipe = cadastrarEquipe.executar(
            new EquipesUseCases.CadastrarEquipe.Comando("Equipe inicial", Set.of(a.id()))
        );
        mvc.perform(
            put("/api/equipes/" + equipe.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Equipe revisada\",\"membrosIds\":[" + b.id() + "]}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Equipe revisada"))
            .andExpect(jsonPath("$.membrosIds[0]").value(b.id()));
        assertEquals(Set.of(b.id()), equipes.porId(equipe.id()).orElseThrow().getMembrosIds());
        mvc.perform(delete("/api/equipes/" + equipe.id()).session(chefe())).andExpect(status().isNoContent());
        assertTrue(equipes.porId(equipe.id()).isEmpty());
        assertEquals(
            0L,
            jdbc.queryForObject(
                "select count(*) from equipe_membro where equipe_id = ?",
                Long.class,
                equipe.id()
            )
        );
        assertNotNull(buscarAdvogado.executar(a.id()));
        assertNotNull(buscarAdvogado.executar(b.id()));
    }

    @Test
    void rejeitaMembroInexistenteSemAlterarEquipe() throws Exception {
        var equipe = cadastrarEquipe.executar(
            new EquipesUseCases.CadastrarEquipe.Comando("Equipe preservada")
        );
        mvc.perform(
            put("/api/equipes/" + equipe.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Nome inválido\",\"membrosIds\":[999999]}")
        ).andExpect(status().isBadRequest());
        assertEquals("Equipe preservada", equipes.porId(equipe.id()).orElseThrow().getNome());
        mvc.perform(delete("/api/equipes/999999").session(chefe())).andExpect(status().isNotFound());
    }

    @Test
    void restringeMutacoesAoChefe() throws Exception {
        var advogado = ApoioDeTesteWeb.sessaoDe(2L, "Advogado", "PE12345", Papel.ADVOGADO);
        mvc.perform(
            post("/api/equipes")
                .session(advogado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Não permitido\"}")
        ).andExpect(status().isForbidden());
        mvc.perform(
            put("/api/advogados/1").session(advogado).contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isForbidden());
    }

    @Test
    void renderizaAbasEModaisDoCorpoJuridico() throws Exception {
        mvc.perform(get("/painel/advogados").session(chefe()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"aba-equipes\"")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"juridico-modal\"")));
    }
}
