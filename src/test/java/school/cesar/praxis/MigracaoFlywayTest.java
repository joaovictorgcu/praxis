package school.cesar.praxis;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.prazo.OrigemVarredura;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.PrerrogativaPrazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.domain.prazo.SituacaoPrazo;
import school.cesar.praxis.domain.prazo.TipoEventoPrazo;
import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.fixtures.RelogioAjustavel;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sobe a aplicacao como em producao no que diz respeito ao esquema: Flyway
 * cria as tabelas a partir de {@code db/migration} e o Hibernate apenas
 * valida ({@code ddl-auto=validate}). O banco e H2 em modo PostgreSQL, entao
 * o que se prova aqui e que a migracao e as entidades concordam em tabelas,
 * colunas e tipos - nao a sintaxe especifica do PostgreSQL real.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:praxis-flyway;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "praxis.dados-exemplo=false"
})
@Import(RelogioAjustavel.Configuracao.class)
class MigracaoFlywayTest {

    private static final PrazosUseCases.Autor ANA = new PrazosUseCases.Autor("PE12345", "Ana Souza", false);

    @Autowired
    private Flyway flyway;
    @Autowired
    private RelogioAjustavel relogio;
    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired
    private DocumentosUseCases.ListarDocumentos listarDocumentos;
    @Autowired
    private DocumentosUseCases.EditarDocumento editarDocumento;
    @Autowired
    private DocumentosUseCases.ConsultarVersoes consultarVersoes;
    @Autowired
    private PrazosUseCases.AbrirPrazo abrirPrazo;
    @Autowired
    private PrazosUseCases.SuspenderPrazo suspenderPrazo;
    @Autowired
    private PrazosUseCases.RetomarPrazo retomarPrazo;
    @Autowired
    private PrazosUseCases.DilatarPrazo dilatarPrazo;
    @Autowired
    private PrazosUseCases.ConsultarPrazo consultarPrazo;
    @Autowired
    private PrazosUseCases.VarrerPrazos varrerPrazos;
    @Autowired
    private PrazosUseCases.ListarVarreduras listarVarreduras;
    @Autowired
    private UsuariosUseCases.Autenticar autenticar;
    @Autowired
    private UsuariosUseCases.CadastrarUsuario cadastrarUsuario;

    @AfterEach
    void relogioDoSistema() {
        relogio.voltarAoSistema();
    }

    @Test
    @DisplayName("migracoes V1 a V8 aplicadas e validadas pelo Hibernate; fluxo basico grava e le")
    void esquemaVersionado() {
        assertEquals(8, flyway.info().applied().length);
        assertEquals("8", flyway.info().current().getVersion().getVersion());

        // Usuarios iniciais entraram pela carga (tabela usuario com senha_provisoria).
        assertNotNull(autenticar.executar(new UsuariosUseCases.Autenticar.Comando(
                "carla.mendes@praxis.adv.br", "praxis123")));
        cadastrarUsuario.executar(new UsuariosUseCases.CadastrarUsuario.Comando(
                "Novo", "novo@praxis.adv.br", "PE11111", Papel.ADVOGADO, "senha123"));

        String numero = "0001111-11.2026.8.17.0011";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente Flyway", "Recife", true, "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "x"), "PE12345"));
        assertEquals(1, listarDocumentos.executar(numero).size());
    }

    @Test
    @DisplayName("V7 no esquema do Flyway: suspensao, retomada, dilacao, trilha e varredura gravam e leem")
    void cicloDeVidaDoPrazoNoEsquemaVersionado() {
        // Dilacao so cabe antes do vencimento: o cenario diz que hoje e 16/09/2026.
        relogio.fixar(LocalDate.of(2026, 9, 16));
        String numero = "0002222-22.2026.8.17.0022";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente V7", "Recife", false, "Ana Souza", "ana@praxis.adv.br", "PE12345"));

        // Intimacao sexta 04/09/2026, 07/09 e feriado nacional: vence 14/09; em dobro (Fazenda), 21/09.
        Prazo prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(numero, "Contestação",
                LocalDate.of(2026, 9, 4), 5, true, RegimeContagem.DIAS_UTEIS, PrerrogativaPrazo.FAZENDA_PUBLICA, ANA));
        assertEquals(LocalDate.of(2026, 9, 21), prazo.getVencimento());

        suspenderPrazo.executar(new PrazosUseCases.SuspenderPrazo.Comando(prazo.getId(),
                LocalDate.of(2026, 9, 10), "Convenção das partes (art. 313, II)", ANA));
        retomarPrazo.executar(new PrazosUseCases.RetomarPrazo.Comando(prazo.getId(), LocalDate.of(2026, 9, 15), ANA));
        Prazo dilatado = dilatarPrazo.executar(new PrazosUseCases.DilatarPrazo.Comando(prazo.getId(), 2,
                "Decisão que dilatou o prazo", ANA));

        // 10 dias uteis (5 em dobro) + 2 de dilacao, sem 10, 11 e 14/09 (suspensos): vence 28/09.
        assertEquals(LocalDate.of(2026, 9, 28), dilatado.getVencimento());
        PrazosUseCases.ConsultarPrazo.DetalhePrazo detalhe = consultarPrazo.executar(prazo.getId());
        assertEquals(SituacaoPrazo.EM_ABERTO, detalhe.situacao());
        assertEquals(LocalDate.of(2026, 9, 21), detalhe.vencimentoOriginal());
        assertEquals(LocalDate.of(2026, 9, 28), detalhe.vencimento());
        assertEquals(12, detalhe.diasEfetivos());
        assertEquals(1, detalhe.suspensoes().size());
        assertEquals(LocalDate.of(2026, 9, 14), detalhe.suspensoes().get(0).fim());
        assertEquals(java.util.List.of(TipoEventoPrazo.ABERTURA, TipoEventoPrazo.SUSPENSAO,
                        TipoEventoPrazo.RETOMADA, TipoEventoPrazo.DILACAO),
                detalhe.eventos().stream().map(e -> e.tipo()).toList());

        // Em 21/09 faltam 5 dias uteis (22, 23, 24, 25 e 28): alerta ATENCAO, gravado na trilha.
        PrazosUseCases.VarrerPrazos.Resultado resultado = varrerPrazos.executar(new PrazosUseCases.VarrerPrazos.Comando(
                LocalDate.of(2026, 9, 21), OrigemVarredura.MANUAL, "Teste Flyway"));
        assertNotNull(resultado.execucao().id());
        assertEquals(1, resultado.alertas().size());
        assertTrue(listarVarreduras.executar(5).stream().anyMatch(v -> "Teste Flyway".equals(v.solicitante())));
        assertEquals(TipoEventoPrazo.ALERTA, consultarPrazo.executar(prazo.getId()).eventos().get(4).tipo());
    }

    @Test
    @DisplayName("V8 no esquema do Flyway: geracao grava a versao 1 e a edicao grava a versao 2")
    void versoesDaPecaNoEsquemaVersionado() {
        String numero = "0003333-33.2026.8.17.0033";
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numero, "Cliente V8", "Recife", false, "Ana Souza", "ana@praxis.adv.br", "PE12345"));
        DocumentoGerado peca = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numero, TipoDocumento.PETICAO_INICIAL, Map.of("fatos", "Fato original."), "PE12345"));

        editarDocumento.executar(new DocumentosUseCases.EditarDocumento.Comando(peca.getId(),
                peca.getConteudo() + "\nParágrafo acrescentado.", "Acrescenta parágrafo", "PE12345", "Ana Souza"));

        var versoes = consultarVersoes.listar(peca.getId());
        assertEquals(2, versoes.size());
        assertTrue(versoes.get(1).atual());
        var comparacao = consultarVersoes.comparar(peca.getId(), 1, 2, "PE12345", null, "Ana Souza");
        assertEquals(1, comparacao.incluidas());
        assertEquals(0, comparacao.removidas());
    }
}
