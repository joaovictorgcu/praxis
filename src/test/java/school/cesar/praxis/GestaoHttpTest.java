package school.cesar.praxis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;
import school.cesar.praxis.presentation.web.seguranca.ProtecaoForcaBruta;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Telas de processos, usuarios e conta, freio de forca bruta no login e os
 * status HTTP da API para transicao invalida e parametro mal formado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApoioDeTesteWeb.class)
class GestaoHttpTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private ProtecaoForcaBruta protecao;
    @Autowired private UsuariosUseCases.CadastrarUsuario cadastrarUsuario;
    @Autowired private UsuariosUseCases.Autenticar autenticar;
    @Autowired private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired private DocumentosUseCases.EnviarDocumentoParaRevisao enviarRevisao;
    @Autowired private DocumentosUseCases.RejeitarDocumento rejeitar;
    @Autowired private DocumentosUseCases.AprovarDocumento aprovar;
    @Autowired private DocumentosUseCases.HabilitarOab habilitarOab;
    @Autowired private DocumentosUseCases.DesfazerDecisaoDocumento desfazer;
    @Autowired private DocumentosUseCases.BaixarDocumento baixar;

    private static MockHttpSession sessao(Long id, String nome, String oab, Papel papel) {
        return ApoioDeTesteWeb.sessaoDe(id, nome, oab, papel);
    }

    private MockHttpSession chefe() {
        Usuario carla = autenticar.executar(new UsuariosUseCases.Autenticar.Comando("carla.mendes@praxis.adv.br", "praxis123"));
        return sessao(carla.getId(), carla.getNome(), carla.getOab(), Papel.CHEFE);
    }

    private MockHttpSession advogada() {
        Usuario ana = autenticar.executar(new UsuariosUseCases.Autenticar.Comando("ana.souza@praxis.adv.br", "praxis123"));
        return sessao(ana.getId(), ana.getNome(), ana.getOab(), Papel.ADVOGADO);
    }

    @Test
    @DisplayName("processo: cadastrar pela tela, abrir ficha, registrar andamento e abrir prazo")
    void fichaDoProcesso() throws Exception {
        MockHttpSession ana = advogada();
        String numero = "0009876-54.2026.8.17.0009";

        mvc.perform(post("/painel/processos").session(ana)
                        .param("numeroCnj", numero)
                        .param("cliente", "Padaria Beta ME")
                        .param("comarca", "Recife")
                        .param("responsavelOab", "PE12345"))
                .andExpect(redirectedUrl("/painel/processos/" + numero))
                .andExpect(flash().attribute("mensagem", containsString("cadastrado")));

        mvc.perform(post("/painel/processos/" + numero + "/andamentos").session(ana)
                        .param("data", "2026-09-01").param("descricao", "Intimação para contestar")
                        .param("tipo", "INTIMACAO"))
                .andExpect(flash().attribute("mensagem", containsString("Andamento")));

        mvc.perform(post("/painel/processos/" + numero + "/prazos").session(ana)
                        .param("descricao", "Contestacao").param("intimacao", "2026-09-01")
                        .param("quantidadeDias", "15").param("fatal", "true").param("regime", "DIAS_UTEIS"))
                .andExpect(flash().attribute("mensagem", containsString("Prazo aberto")));

        mvc.perform(get("/painel/processos/" + numero).session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Padaria Beta ME")))
                .andExpect(content().string(containsString("Intimação para contestar")))
                .andExpect(content().string(containsString("Contestacao")))
                .andExpect(content().string(containsString("em aberto")));

        mvc.perform(get("/painel/processos").param("busca", "padaria").session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(numero)));

        // Data invalida vira mensagem, nao 500.
        mvc.perform(post("/painel/processos/" + numero + "/andamentos").session(ana)
                        .param("data", "01/09/2026").param("descricao", "x").param("tipo", "OUTRO"))
                .andExpect(flash().attribute("erro", containsString("data inválida")));

        // CNJ inexistente na ficha volta para a lista com erro.
        mvc.perform(get("/painel/processos/0000000-00.2026.8.17.0000").session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("não encontrado")));
    }

    @Test
    @DisplayName("usuarios: tela e cadastro sao do chefe; nao remove a si mesmo")
    void gestaoDeUsuarios() throws Exception {
        mvc.perform(get("/painel/usuarios").session(advogada())).andExpect(status().isForbidden());

        MockHttpSession carla = chefe();
        mvc.perform(get("/painel/usuarios").session(carla))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("bruno.carvalho@praxis.adv.br")));

        mvc.perform(post("/painel/usuarios").session(carla)
                        .param("nome", "Davi Lima").param("email", "davi@praxis.adv.br")
                        .param("oab", "PE77777").param("papel", "ADVOGADO").param("senha", "davi1234"))
                .andExpect(flash().attribute("mensagem", containsString("Davi Lima")));

        mvc.perform(post("/painel/usuarios").session(carla)
                        .param("nome", "Outro").param("email", "davi@praxis.adv.br")
                        .param("oab", "PE77778").param("papel", "ADVOGADO").param("senha", "outro123"))
                .andExpect(flash().attribute("erro", containsString("já existe usuário com o e-mail")));

        Long idCarla = UsuarioLogado.da(carla).id();
        mvc.perform(post("/painel/usuarios/" + idCarla + "/remover").session(carla))
                .andExpect(flash().attribute("erro", containsString("próprio usuário")));
    }

    @Test
    @DisplayName("conta: trocar senha exige a atual, encerra a sessao e a nova passa a valer")
    void trocarSenha() throws Exception {
        Usuario davi = cadastrarUsuario.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Elisa Rocha", "elisa@praxis.adv.br", "PE88888", Papel.ADVOGADO, "elisa123"));
        MockHttpSession elisa = sessao(davi.getId(), davi.getNome(), davi.getOab(), Papel.ADVOGADO);

        mvc.perform(get("/painel/conta").session(elisa))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PE88888")));

        mvc.perform(post("/painel/conta/senha").session(elisa)
                        .param("senhaAtual", "errada!").param("novaSenha", "nova1234").param("confirmacao", "nova1234"))
                .andExpect(redirectedUrl("/painel/conta"))
                .andExpect(flash().attribute("erro", containsString("senha atual não confere")));

        mvc.perform(post("/painel/conta/senha").session(elisa)
                        .param("senhaAtual", "elisa123").param("novaSenha", "nova1234").param("confirmacao", "diferente"))
                .andExpect(flash().attribute("erro", containsString("confirmação")));

        mvc.perform(post("/painel/conta/senha").session(elisa)
                        .param("senhaAtual", "elisa123").param("novaSenha", "nova1234").param("confirmacao", "nova1234"))
                .andExpect(redirectedUrl("/login"));
        assertTrue(elisa.isInvalid());

        assertNotNull(autenticar.executar(new UsuariosUseCases.Autenticar.Comando("elisa@praxis.adv.br", "nova1234")));
    }

    @Test
    @DisplayName("login: apos 5 falhas o e-mail fica bloqueado por um intervalo")
    void forcaBruta() throws Exception {
        String email = "bruno.carvalho@praxis.adv.br";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/login").param("email", email).param("senha", "errada" + i))
                    .andExpect(status().isOk());
        }
        assertTrue(protecao.bloqueado(email));
        // Ate a senha certa e recusada enquanto durar o bloqueio.
        mvc.perform(post("/login").param("email", email).param("senha", "praxis123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("muitas tentativas")));
        protecao.registrarSucesso(email); // limpa para nao afetar outros testes
        assertFalse(protecao.bloqueado(email));
    }

    @Test
    @DisplayName("API: transicao invalida responde 409 e parametro mal formado 400, nunca 500")
    void statusDaApi() throws Exception {
        String numero = "0008765-43.2026.8.17.0008";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Gama", "Recife", false, "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        DocumentoGerado doc = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "x"), "PE12345"));

        MockHttpSession carla = chefe();

        // Aprovar rascunho: estado invalido -> 409
        mvc.perform(post("/api/documentos/" + doc.getId() + "/aprovar").session(carla)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"ok\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro", containsString("RASCUNHO")));

        // Leitura da API segue aberta: sem sessao, o 400 vem do parametro, nao da guarda.
        mvc.perform(get("/api/prazos/agenda").param("ate", "14/09/2026"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/documentos/" + doc.getId() + "/desfazer").session(carla))
                .andExpect(status().isConflict());

        // Contrato fixo nao informa horas: campo ausente nao pode virar 400 de desserializacao.
        mvc.perform(post("/api/honorarios").session(carla)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numeroProcesso\":\"" + numero + "\",\"modalidade\":\"FIXO\","
                                + "\"celebradoEm\":\"2026-09-01\",\"valorFixo\":8500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modalidade").value("FIXO"));
    }

    @Test
    @DisplayName("historico com ';' e '|' no comentario sobrevive ao banco; desfazer nao perde OAB habilitada")
    void historicoEDesfazer() {
        String numero = "0007654-32.2026.8.17.0077";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Delta", "Recife", true, "Bruno Carvalho", "bruno@praxis.adv.br", "PE54321"));
        DocumentoGerado doc = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "x"), "PE54321"));
        Long id = doc.getId();

        enviarRevisao.executar(new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(id));
        rejeitar.executar(new DocumentosUseCases.RejeitarDocumento.Comando(id, "PE00001", "faltou procuração; refazer | urgente"));

        DocumentoGerado lido = baixar.executar(id, "PE54321");
        assertEquals("REJEITADO", lido.getStatus().nome());
        assertEquals("faltou procuração; refazer | urgente",
                lido.getHistorico().get(lido.getHistorico().size() - 1).getComentario());

        // Habilita OAB depois da decisao; desfazer deve preservar.
        habilitarOab.executar(new DocumentosUseCases.HabilitarOab.Comando(id, "PE99999"));
        DocumentoGerado desfeito = desfazer.executar(new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(id));
        assertEquals("EM_REVISAO", desfeito.getStatus().nome());
        assertTrue(desfeito.getOabsHabilitadas().contains("PE99999"));
        assertNotNull(baixar.executar(id, "PE99999"));

        // Aprovado -> desfazer tambem volta a revisao; desfazer em revisao e invalido.
        aprovar.executar(new DocumentosUseCases.AprovarDocumento.Comando(id, "PE00001", "de acordo"));
        desfazer.executar(new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(id));
        assertThrows(IllegalStateException.class, () ->
                desfazer.executar(new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(id)));
    }

    @Test
    @DisplayName("gerar peca por modelo: campos chegam como campo_<nome> e a tela expoe os campos esperados")
    void camposDoModeloPelaTela() throws Exception {
        MockHttpSession ana = advogada();

        mvc.perform(get("/painel/documentos").session(ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-campos=\"")))
                .andExpect(content().string(containsString("id=\"campos-modelo\"")))
                .andExpect(content().string(not(containsString("camposLivres"))));

        mvc.perform(post("/painel/documentos").session(ana)
                        .param("numeroProcesso", "0001234-56.2026.8.17.0001")
                        .param("tipo", "PETICAO_INICIAL")
                        .param("codigoModelo", "COBRANCA_ALUGUEL")
                        .param("campo_valorDivida", "R$ 12.500,00")
                        .param("campo_enderecoImovel", "Rua das Flores, 10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("acumulando R$ 12.500,00")))
                .andExpect(content().string(containsString("Rua das Flores, 10")))
                .andExpect(content().string(not(containsString("a preencher)"))));
    }

    @Test
    @DisplayName("painel: resumo do dia aparece e o nome no cabecalho leva a minha conta")
    void resumoDoPainel() throws Exception {
        mvc.perform(get("/painel").session(chefe()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prazos Vencidos")))
                .andExpect(content().string(containsString("Aguardando Sua Revisão")))
                .andExpect(content().string(containsString("/painel/conta")))
                .andExpect(content().string(containsString("/painel/usuarios")));

        mvc.perform(get("/painel").session(advogada()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/painel/usuarios"))));

        mvc.perform(get("/painel").param("ate", "hoje").session(advogada()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data inválida")));
    }
}
