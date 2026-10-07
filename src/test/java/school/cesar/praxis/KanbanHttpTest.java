package school.cesar.praxis;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.KanbanUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.out.KanbanRepositorio;
import school.cesar.praxis.domain.kanban.ItemChecklist;
import school.cesar.praxis.domain.kanban.PrioridadeTarefa;
import school.cesar.praxis.domain.kanban.Tarefa;
import school.cesar.praxis.domain.usuario.Papel;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
@TestPropertySource(properties = "praxis.dados-exemplo=false")
@Transactional
class KanbanHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private KanbanUseCases.ConsultarQuadro consultar;

    @Autowired
    private KanbanUseCases.CriarTarefa criar;

    @Autowired
    private KanbanUseCases.ConfigurarColunas configurar;

    @Autowired
    private KanbanRepositorio repositorio;

    @Autowired
    private ProcessosUseCases.CadastrarProcesso processos;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private static final AtomicInteger SEQUENCIA = new AtomicInteger(9000000);
    private final KanbanUseCases.Acesso acesso = new KanbanUseCases.Acesso(true, "ADMIN");

    private MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Administrador", "ADMIN", Papel.CHEFE);
    }

    private String processo(boolean sigiloso) {
        String numero = SEQUENCIA.incrementAndGet() + "-01.2026.8.17.0001";
        processos.executar(
            new ProcessosUseCases.CadastrarProcesso.Comando(
                numero,
                "Cliente Kanban",
                "Recife",
                sigiloso,
                "Responsável",
                "responsavel@teste.adv.br",
                "PE99999"
            )
        );
        return numero;
    }

    private Tarefa tarefa(String numero, Long coluna, String titulo) {
        return criar.executar(
            new KanbanUseCases.CriarTarefa.Comando(
                numero,
                coluna,
                titulo,
                "Descrição",
                PrioridadeTarefa.ALTA,
                null,
                null,
                List.of(new ItemChecklist("Conferir documentos", false))
            ),
            acesso
        );
    }

    private List<KanbanUseCases.ConfigurarColunas.Coluna> configuracao(KanbanUseCases.Quadro quadro) {
        return quadro
            .colunas()
            .stream()
            .map(c -> new KanbanUseCases.ConfigurarColunas.Coluna(c.id(), c.nome(), c.cor(), c.conclusiva()))
            .toList();
    }

    @Test
    void criaEditaEAtualizaChecklistComControleDeVersao() throws Exception {
        String numero = processo(false);
        Long coluna = consultar.executar(acesso).colunas().get(0).id();
        mvc.perform(
            post("/api/kanban/tarefas")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numeroProcesso\":\"" +
                        numero +
                        "\",\"colunaId\":" +
                        coluna +
                        ",\"titulo\":\"Criada por HTTP\",\"prioridade\":\"NORMAL\"}"
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Criada por HTTP"));
        Tarefa t = tarefa(numero, coluna, "Tarefa original");
        mvc.perform(
            put("/api/kanban/tarefas/" + t.id())
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numeroProcesso\":\"" +
                        numero +
                        "\",\"colunaId\":" +
                        coluna +
                        ",\"titulo\":\"Tarefa revisada\",\"prioridade\":\"URGENTE\",\"vencimento\":\"2026-10-20\",\"checklist\":[{\"texto\":\"Revisar peça\",\"concluido\":false}],\"versao\":" +
                        t.versao() +
                        "}"
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Tarefa revisada"))
            .andExpect(jsonPath("$.prioridade").value("URGENTE"));
        Tarefa salva = repositorio.tarefaPorId(t.id()).orElseThrow();
        assertEquals(t.criadaEm(), salva.criadaEm());
        mvc.perform(
            put("/api/kanban/tarefas/" + t.id() + "/checklist")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"checklist\":[{\"texto\":\"Revisar peça\",\"concluido\":true}],\"versao\":" +
                        salva.versao() +
                        "}"
                )
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.checklist[0].concluido").value(true));
        mvc.perform(
            put("/api/kanban/tarefas/" + t.id() + "/mover")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"colunaId\":" + coluna + ",\"versao\":" + t.versao() + "}")
        ).andExpect(status().isConflict());
    }

    @Test
    void moveEntreColunasENaMesmaColunaSemPerderAOrdem() throws Exception {
        var colunas = consultar.executar(acesso).colunas();
        String numero = processo(false);
        Tarefa a = tarefa(numero, colunas.get(0).id(), "Primeira");
        Tarefa b = tarefa(numero, colunas.get(0).id(), "Segunda");
        Tarefa c = tarefa(numero, colunas.get(1).id(), "Terceira");
        mvc.perform(
            put("/api/kanban/tarefas/" + b.id() + "/mover")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"colunaId\":" +
                        c.colunaId() +
                        ",\"antesDeId\":" +
                        c.id() +
                        ",\"versao\":" +
                        b.versao() +
                        "}"
                )
        ).andExpect(status().isOk());
        assertEquals(0, repositorio.tarefaPorId(b.id()).orElseThrow().ordem());
        Tarefa atual = repositorio.tarefaPorId(c.id()).orElseThrow();
        assertEquals(1, atual.ordem());
        mvc.perform(
            put("/api/kanban/tarefas/" + c.id() + "/mover")
                .session(chefe())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"colunaId\":" +
                        c.colunaId() +
                        ",\"antesDeId\":" +
                        b.id() +
                        ",\"versao\":" +
                        atual.versao() +
                        "}"
                )
        ).andExpect(status().isOk());
        assertEquals(0, repositorio.tarefaPorId(c.id()).orElseThrow().ordem());
        assertEquals(1, repositorio.tarefaPorId(b.id()).orElseThrow().ordem());
        assertEquals(0, repositorio.tarefaPorId(a.id()).orElseThrow().ordem());
    }

    @Test
    void personalizaCriaEReordenaColunasPersistindoTodosOsCampos() {
        var quadro = consultar.executar(acesso);
        var colunas = new ArrayList<>(configuracao(quadro));
        colunas.remove(0);
        colunas.add(
            new KanbanUseCases.ConfigurarColunas.Coluna(
                quadro.colunas().get(0).id(),
                "Planejamento",
                "#12ab34",
                true
            )
        );
        colunas.add(0, new KanbanUseCases.ConfigurarColunas.Coluna(null, "Triagem", "#334455", false));
        configurar.executar(
            new KanbanUseCases.ConfigurarColunas.Comando(colunas, Map.of(), quadro.revisao()),
            acesso
        );
        var salvo = consultar.executar(acesso);
        assertEquals("Triagem", salvo.colunas().get(0).nome());
        assertNotNull(salvo.colunas().get(0).id());
        var editada = salvo.colunas().get(salvo.colunas().size() - 1);
        assertEquals("Planejamento", editada.nome());
        assertEquals("#12ab34", editada.cor());
        assertTrue(editada.conclusiva());
        for (int i = 0; i < salvo.colunas().size(); i++) {
            assertEquals(i, salvo.colunas().get(i).ordem());
        }
        assertThrows(IllegalStateException.class, () ->
            configurar.executar(
                new KanbanUseCases.ConfigurarColunas.Comando(colunas, Map.of(), quadro.revisao()),
                acesso
            )
        );
    }

    @Test
    void removeColunaTransferindoTarefasSemExcluirDados() {
        var quadro = consultar.executar(acesso);
        var origem = quadro.colunas().get(0);
        var destino = quadro.colunas().get(1);
        Tarefa t = tarefa(processo(false), origem.id(), "Preservar checklist");
        var colunas = configuracao(quadro)
            .stream()
            .filter(c -> !c.id().equals(origem.id()))
            .toList();
        assertThrows(IllegalArgumentException.class, () ->
            configurar.executar(
                new KanbanUseCases.ConfigurarColunas.Comando(colunas, Map.of(), quadro.revisao()),
                acesso
            )
        );
        assertEquals(origem.id(), repositorio.tarefaPorId(t.id()).orElseThrow().colunaId());
        configurar.executar(
            new KanbanUseCases.ConfigurarColunas.Comando(
                colunas,
                Map.of(origem.id(), destino.id()),
                quadro.revisao()
            ),
            acesso
        );
        Tarefa salva = repositorio.tarefaPorId(t.id()).orElseThrow();
        assertEquals(destino.id(), salva.colunaId());
        assertEquals(t.checklist(), salva.checklist());
        assertTrue(
            repositorio
                .listarColunas()
                .stream()
                .noneMatch(c -> c.id().equals(origem.id()))
        );
        assertThrows(IllegalArgumentException.class, () ->
            configurar.executar(
                new KanbanUseCases.ConfigurarColunas.Comando(
                    List.of(),
                    Map.of(),
                    consultar.executar(acesso).revisao()
                ),
                acesso
            )
        );
    }

    @Test
    void excluiTarefaEChecklistENormalizaPosicoes() throws Exception {
        Long coluna = consultar.executar(acesso).colunas().get(0).id();
        String numero = processo(false);
        Tarefa a = tarefa(numero, coluna, "Excluir");
        Tarefa b = tarefa(numero, coluna, "Preservar");
        mvc.perform(
            delete("/api/kanban/tarefas/" + a.id())
                .param("versao", String.valueOf(a.versao()))
                .session(chefe())
        ).andExpect(status().isNoContent());
        assertTrue(repositorio.tarefaPorId(a.id()).isEmpty());
        assertEquals(
            0L,
            jdbc.queryForObject(
                "select count(*) from kanban_checklist where tarefa_id = ?",
                Long.class,
                a.id()
            )
        );
        assertEquals(0, repositorio.tarefaPorId(b.id()).orElseThrow().ordem());
    }

    @Test
    void exigeSessaoRespeitaSigiloERestringeConfiguracaoAoChefe() throws Exception {
        var advogado = ApoioDeTesteWeb.sessaoDe(2L, "Advogado", "PE12345", Papel.ADVOGADO);
        Long coluna = consultar.executar(acesso).colunas().get(0).id();
        Tarefa sigilosa = tarefa(processo(true), coluna, "Tarefa sigilosa");
        mvc.perform(get("/api/kanban")).andExpect(status().isUnauthorized());
        var restrito = consultar.executar(new KanbanUseCases.Acesso(false, "PE12345"));
        assertTrue(
            restrito
                .tarefas()
                .stream()
                .noneMatch(t -> t.id().equals(sigilosa.id()))
        );
        assertTrue(
            restrito
                .processos()
                .stream()
                .noneMatch(p -> p.numero().equals(sigilosa.numeroProcesso()))
        );
        mvc.perform(
            put("/api/kanban/tarefas/" + sigilosa.id() + "/mover")
                .session(advogado)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"colunaId\":" + coluna + ",\"versao\":" + sigilosa.versao() + "}")
        ).andExpect(status().isForbidden());
        mvc.perform(
            put("/api/kanban/colunas").session(advogado).contentType(MediaType.APPLICATION_JSON).content("{}")
        ).andExpect(status().isForbidden());
        mvc.perform(get("/painel/kanban").session(chefe()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"kb-colunas\"")));
    }
}
