package school.cesar.praxis;

import org.junit.jupiter.api.AfterEach;
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
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.fixtures.RelogioAjustavel;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contrato HTTP do ciclo de vida do prazo: a ficha do prazo, as acoes pela tela
 * (POST-redirect-GET com mensagem) e a API REST. Banco proprio, para a classe
 * nao dividir tabelas com os outros contextos de teste, e "hoje" fixado por
 * teste, porque suspensao, retomada e dilacao dependem da data corrente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({ApoioDeTesteWeb.class, RelogioAjustavel.Configuracao.class})
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:praxis-prazo-http;DB_CLOSE_DELAY=-1")
class PrazoHttpTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private PrazosUseCases.AbrirPrazo abrirPrazo;
    @Autowired
    private PrazosUseCases.ConsultarPrazo consultarPrazo;
    @Autowired
    private RelogioAjustavel relogio;

    private static MockHttpSession ana() {
        return ApoioDeTesteWeb.sessaoDe(2L, "Ana Souza", "PE12345", Papel.ADVOGADO);
    }

    private static MockHttpSession bruno() {
        return ApoioDeTesteWeb.sessaoDe(3L, "Bruno Carvalho", "PE54321", Papel.ADVOGADO);
    }

    @AfterEach
    void relogioDoSistema() {
        relogio.voltarAoSistema();
    }

    /** Intimacao 04/09/2026 (sexta), 07/09 e feriado nacional: vence 14/09. Responsavel: Ana. */
    private Prazo embargos(String numero) {
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Joana Ferreira Lima", "Recife", false, "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        return abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(numero, "Embargos de declaração",
                LocalDate.of(2026, 9, 4), 5, true, RegimeContagem.DIAS_UTEIS));
    }

    @Test
    @DisplayName("ficha do prazo mostra contagem, vencimentos, trilha e as acoes do estado; cancelar so para o responsavel")
    void fichaDoPrazo() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 10));
        Prazo prazo = embargos("0010001-01.2026.8.17.0001");

        mvc.perform(get("/painel/prazos/" + prazo.getId()).session(ana()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trilha do Prazo")))
                .andExpect(content().string(containsString("Vencimento Original")))
                .andExpect(content().string(containsString("14/09/2026")))
                .andExpect(content().string(containsString("Abertura")))
                .andExpect(content().string(containsString("Suspender Prazo")))
                .andExpect(content().string(containsString("Cancelar Prazo")));

        mvc.perform(get("/painel/prazos/" + prazo.getId()).session(bruno()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Cancelar Prazo"))))
                .andExpect(content().string(containsString("ficam com o responsável")));
    }

    @Test
    @DisplayName("suspender e retomar pela tela: o saldo congela e volta a correr com o vencimento recalculado")
    void suspenderERetomarPelaTela() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 16));
        Prazo prazo = embargos("0010002-02.2026.8.17.0001");
        String ficha = "/painel/prazos/" + prazo.getId();

        mvc.perform(post(ficha + "/suspender").session(ana())
                        .param("inicio", "2026-09-10")
                        .param("motivo", "Convenção das partes (art. 313, II)"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ficha))
                .andExpect(flash().attribute("mensagem", containsString("suspenso desde 10/09/2026")));
        assertEquals(SituacaoPrazo.SUSPENSO, consultarPrazo.executar(prazo.getId()).situacao());

        mvc.perform(get(ficha).session(ana()))
                .andExpect(content().string(containsString("Saldo Congelado")))
                .andExpect(content().string(containsString("3 dias úteis")));

        mvc.perform(post(ficha + "/retomar").session(ana()).param("retomada", "2026-09-16"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensagem", containsString("18/09/2026")));
        assertEquals(LocalDate.of(2026, 9, 18), consultarPrazo.executar(prazo.getId()).vencimento());
    }

    @Test
    @DisplayName("cancelar o prazo de outro advogado volta com erro e nao muda nada")
    void cancelarDeOutroAdvogado() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 10));
        Prazo prazo = embargos("0010003-03.2026.8.17.0001");

        mvc.perform(post("/painel/prazos/" + prazo.getId() + "/cancelar").session(bruno())
                        .param("motivo", "Engano"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erro", containsString("responsável")));
        assertEquals(SituacaoPrazo.EM_ABERTO, consultarPrazo.executar(prazo.getId()).situacao());
    }

    @Test
    @DisplayName("API: dilatar, ler a ficha com a trilha e rodar a varredura que fica no historico")
    void apiDoCicloDeVida() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 10));
        Prazo prazo = embargos("0010004-04.2026.8.17.0001");

        mvc.perform(post("/api/prazos/" + prazo.getId() + "/dilatar").session(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dias\": 3, \"fundamento\": \"Decisão de 10/09/2026 dilatou o prazo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vencimento").value("2026-09-17"))
                .andExpect(jsonPath("$.vencimentoOriginal").value("2026-09-14"))
                .andExpect(jsonPath("$.diasEfetivos").value(8));

        mvc.perform(get("/api/prazos/" + prazo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos.length()").value(2))
                .andExpect(jsonPath("$.eventos[1].tipo").value("DILACAO"))
                .andExpect(jsonPath("$.eventos[1].autorNome").value("Ana Souza"));

        mvc.perform(post("/api/prazos/varredura").session(ana()).param("hoje", "2026-09-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.execucao").isNumber());

        mvc.perform(get("/api/prazos/varreduras").param("limite", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].solicitante").value("Ana Souza"))
                .andExpect(jsonPath("$[0].origem").value("MANUAL"))
                .andExpect(jsonPath("$[0].dataReferencia").value("2026-09-14"));
    }

    @Test
    @DisplayName("API: dilacao depois do vencimento responde 409 citando o art. 139")
    void dilacaoTardia() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 16));
        Prazo prazo = embargos("0010005-05.2026.8.17.0001");

        mvc.perform(post("/api/prazos/" + prazo.getId() + "/dilatar").session(ana())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dias\": 3, \"fundamento\": \"Pedido tardio\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(containsString("139")));
    }

    @Test
    @DisplayName("abrir prazo em dobro pela ficha do processo informa o vencimento calculado")
    void abrirPrazoEmDobro() throws Exception {
        String numero = "0010006-06.2026.8.17.0001";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Joana Ferreira Lima", "Recife", false, "Ana Souza", "ana@praxis.adv.br", "PE12345"));

        mvc.perform(post("/painel/processos/" + numero + "/prazos").session(ana())
                        .param("descricao", "Contestação da Fazenda")
                        .param("intimacao", "2026-09-04")
                        .param("quantidadeDias", "5")
                        .param("fatal", "true")
                        .param("regime", "DIAS_UTEIS")
                        .param("prerrogativa", "FAZENDA_PUBLICA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensagem", containsString("vence em 21/09/2026")));
    }

    @Test
    @DisplayName("varredura pela agenda grava a execucao e a agenda mostra quem rodou")
    void varreduraPelaAgenda() throws Exception {
        relogio.fixar(LocalDate.of(2026, 9, 10));
        embargos("0010007-07.2026.8.17.0001");

        mvc.perform(post("/painel/varredura").session(bruno()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensagem", containsString("Varredura concluída")));

        mvc.perform(get("/painel").session(bruno()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Últimas Varreduras")))
                .andExpect(content().string(containsString("Bruno Carvalho")));
    }
}
