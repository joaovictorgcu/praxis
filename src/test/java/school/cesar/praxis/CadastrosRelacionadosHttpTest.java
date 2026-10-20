package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.application.port.in.ClienteUseCase;
import school.cesar.praxis.application.port.in.ParteContrariaUseCase;
import school.cesar.praxis.domain.usuario.Papel;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class CadastrosRelacionadosHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private ClienteUseCase clientes;
    @Autowired private ParteContrariaUseCase partes;
    @Autowired private AgendaDeAudienciasUseCase audiencias;

    private MockHttpSession chefe() {
        return ApoioDeTesteWeb.sessaoDe(1L, "Carla Mendes", "PE00001", Papel.CHEFE);
    }

    private MockHttpSession advogado() {
        return ApoioDeTesteWeb.sessaoDe(2L, "Ana Souza", "PE00002", Papel.ADVOGADO);
    }

    @Test
    @DisplayName("as tres telas de gestao abrem para o chefe e sao bloqueadas para advogado")
    void telasRestritasAoChefe() throws Exception {
        for (String rota : new String[]{
                "/painel/cadastros/clientes",
                "/painel/cadastros/partes-contrarias",
                "/painel/cadastros/audiencias"}) {
            mvc.perform(get(rota).session(chefe())).andExpect(status().isOk());
            mvc.perform(get(rota).session(advogado())).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("chefe cria edita desativa e reativa um cliente pela interface")
    void gerenciaClientePelaTela() throws Exception {
        String documento = "GESTAO-" + System.nanoTime();
        mvc.perform(post("/painel/cadastros/clientes").session(chefe())
                        .param("nome", "Cliente manual")
                        .param("cpfOuCnpj", documento)
                        .param("tipoPessoa", "FISICA")
                        .param("email", "cliente@example.test"))
                .andExpect(status().is3xxRedirection());

        var criado = clientes.consultarPorCpfOuCnpj(documento);
        assertTrue(criado.isAtivo());
        assertEquals("cliente@example.test", criado.getEmail());

        mvc.perform(post("/painel/cadastros/clientes/" + criado.getId() + "/editar").session(chefe())
                        .param("nome", "Cliente atualizado")
                        .param("email", "novo@example.test"))
                .andExpect(status().is3xxRedirection());
        assertEquals("Cliente atualizado", clientes.consultarPorCpfOuCnpj(documento).getNome());

        mvc.perform(post("/painel/cadastros/clientes/" + criado.getId() + "/desativar").session(chefe()))
                .andExpect(status().is3xxRedirection());
        assertTrue(clientes.listarTodosOsClientes().stream()
                .filter(cliente -> cliente.getId().equals(criado.getId()))
                .noneMatch(cliente -> cliente.isAtivo()));

        mvc.perform(post("/painel/cadastros/clientes/" + criado.getId() + "/reativar").session(chefe()))
                .andExpect(status().is3xxRedirection());
        assertTrue(clientes.consultarPorCpfOuCnpj(documento).isAtivo());
    }

    @Test
    @DisplayName("as telas exibem o formulário de edição e as ações manuais")
    void formulariosDeGestaoRenderizam() throws Exception {
        mvc.perform(get("/painel/cadastros/clientes").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar cliente")));
        mvc.perform(get("/painel/cadastros/partes-contrarias").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar parte contrária")));
        mvc.perform(get("/painel/cadastros/audiencias").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Agendar audiência")));
    }

    @Test
    @DisplayName("chefe cadastra partes contrárias e audiências pela interface")
    void criaPartesEAudienciasPelaTela() throws Exception {
        String documento = "GESTAO-PARTE-" + System.nanoTime();
        mvc.perform(post("/painel/cadastros/partes-contrarias").session(chefe())
                        .param("nome", "Parte contrária manual")
                        .param("cpfOuCnpj", documento)
                        .param("tipoPessoa", "JURIDICA"))
                .andExpect(status().is3xxRedirection());
        assertTrue(partes.listarTodasAsPartesContrarias().stream()
                .anyMatch(parte -> documento.equals(parte.getCpfOuCnpj())));

        LocalDateTime inicio = LocalDateTime.of(2031, 7, 8, 9, 0);
        LocalDateTime fim = inicio.plusHours(1);
        mvc.perform(post("/painel/cadastros/audiencias").session(chefe())
                        .param("numeroProcesso", "GESTAO-2031-" + System.nanoTime())
                        .param("nomeParteAutora", "Parte autora manual")
                        .param("dataHoraInicio", "2031-07-08T09:00")
                        .param("dataHoraFim", "2031-07-08T10:00")
                        .param("sala", "Sala de gestão"))
                .andExpect(status().is3xxRedirection());
        var criada = audiencias.listarAudienciasPorPeriodo(inicio, fim).stream()
                .filter(audiencia -> "Parte autora manual".equals(audiencia.getNomeParteAutora()))
                .findFirst().orElseThrow();
        assertTrue(criada.isAtiva());

        mvc.perform(get("/painel/cadastros/audiencias").session(chefe())
                        .param("editar", criada.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Editar audiência")))
                .andExpect(content().string(containsString("2031-07-08T09:00")));

        mvc.perform(post("/painel/cadastros/audiencias/" + criada.getId() + "/editar").session(chefe())
                        .param("nomeParteAutora", "Parte autora editada")
                        .param("dataHoraInicio", "2031-07-08T11:00")
                        .param("dataHoraFim", "2031-07-08T12:00")
                        .param("sala", "Sala de gestão"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/painel/cadastros/audiencias/" + criada.getId() + "/cancelar").session(chefe()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/painel/cadastros/audiencias/" + criada.getId() + "/reativar").session(chefe()))
                .andExpect(status().is3xxRedirection());
        assertTrue(audiencias.consultarAudiencia(criada.getId()).isAtiva());

        mvc.perform(post("/painel/cadastros/audiencias").session(chefe())
                        .param("numeroProcesso", "GESTAO-CONFLITO-" + System.nanoTime())
                        .param("nomeParteAutora", "Conflito de sala")
                        .param("dataHoraInicio", "2031-07-08T11:30")
                        .param("dataHoraFim", "2031-07-08T12:30")
                        .param("sala", "Sala de gestão"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("erro"));
    }
}
