package school.cesar.praxis.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import school.cesar.praxis.domain.acesso.AcessoAoProcesso;
import school.cesar.praxis.domain.acesso.SolicitacaoAcesso;
import school.cesar.praxis.domain.acesso.StatusSolicitacao;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.anexo.ArquivoProxy;
import school.cesar.praxis.domain.anexo.TipoArquivo;
import school.cesar.praxis.domain.compartilhado.*;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade do controle de acesso aos autos: a politica de leitura
 * usada pelo Proxy, a concessao de acesso ao processo (expiracao, regra do
 * ultimo acesso), a maquina de estados da solicitacao e a auditoria que o
 * Proxy registra em toda tentativa de leitura.
 */
class ControleDeAcessoTest {

    private final NumeroCnj processo = NumeroCnj.de("0001234-56.2026.8.17.0001");

    // --- Politica de leitura: publico, lista da peca, acesso ao processo ---

    @Test
    @DisplayName("politica: processo publico pode ser lido por qualquer OAB, sem precisar de acesso")
    void processoPublicoSempreLeitura() {
        ArquivoAnexo publico = anexo(false, Set.of());

        assertTrue(PoliticaDeLeituraDosAutos.podeLer(publico, "PE99999", semAcesso(), LocalDate.now()));
    }

    @Test
    @DisplayName("politica: sigiloso com OAB na lista da peca pode ler, mesmo sem acesso ao processo")
    void sigilosoComOabNaListaDaPeca() {
        ArquivoAnexo sigiloso = anexo(true, Set.of("PE12345"));

        assertTrue(PoliticaDeLeituraDosAutos.podeLer(sigiloso, "PE12345", semAcesso(), LocalDate.now()));
    }

    @Test
    @DisplayName("politica: sigiloso sem OAB na lista e sem acesso ao processo e negado")
    void sigilosoSemListaESemAcesso() {
        ArquivoAnexo sigiloso = anexo(true, Set.of("PE12345"));

        assertFalse(PoliticaDeLeituraDosAutos.podeLer(sigiloso, "PE99999", semAcesso(), LocalDate.now()));
    }

    @Test
    @DisplayName("politica: sigiloso sem OAB na lista, mas com acesso ativo ao processo, pode ler")
    void sigilosoSemListaComAcessoAoProcesso() {
        ArquivoAnexo sigiloso = anexo(true, Set.of("PE12345"));
        VerificadorDeAcessoAoProcesso comAcesso = (proc, oab, hoje) -> proc.equals(processo) && "PE99999".equals(oab);

        assertTrue(PoliticaDeLeituraDosAutos.podeLer(sigiloso, "PE99999", comAcesso, LocalDate.now()));
    }

    @Test
    @DisplayName("politica: sem OAB nenhuma, mesmo com verificador, e negado")
    void semOabENegado() {
        ArquivoAnexo sigiloso = anexo(true, Set.of("PE12345"));

        assertFalse(PoliticaDeLeituraDosAutos.podeLer(sigiloso, null, semAcesso(), LocalDate.now()));
        assertFalse(PoliticaDeLeituraDosAutos.podeLer(sigiloso, "  ", semAcesso(), LocalDate.now()));
    }

    // --- AcessoAoProcesso: ativo, expirado, dias para expirar ---

    @Test
    @DisplayName("acesso permanente (sem expiraEm) esta sempre ativo, nao expira")
    void acessoPermanenteSempreAtivo() {
        AcessoAoProcesso acesso = new AcessoAoProcesso(processo, "PE99999", "PE12345", LocalDate.of(2026, 1, 1), null);

        assertTrue(acesso.ativo(LocalDate.of(2030, 1, 1)));
        assertFalse(acesso.expirado(LocalDate.of(2030, 1, 1)));
        assertNull(acesso.diasParaExpirar(LocalDate.now()));
    }

    @Test
    @DisplayName("acesso temporario fica inativo e expirado apos a data de expiracao")
    void acessoTemporarioExpira() {
        AcessoAoProcesso acesso = new AcessoAoProcesso(processo, "PE99999", "PE12345",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(acesso.ativo(LocalDate.of(2026, 1, 31)), "ainda ativo no proprio dia da expiracao");
        assertFalse(acesso.ativo(LocalDate.of(2026, 2, 1)), "inativo no dia seguinte");
        assertTrue(acesso.expirado(LocalDate.of(2026, 2, 1)));
        assertEquals(5L, acesso.diasParaExpirar(LocalDate.of(2026, 1, 26)));
        assertEquals(-1L, acesso.diasParaExpirar(LocalDate.of(2026, 2, 1)));
    }

    @Test
    @DisplayName("revogar exige motivo e nao pode ser feito duas vezes")
    void revogarExigeMotivoENaoDuplicaRevogacao() {
        AcessoAoProcesso acesso = new AcessoAoProcesso(processo, "PE99999", "PE12345", LocalDate.of(2026, 1, 1), null);

        assertThrows(IllegalArgumentException.class, () -> acesso.revogar(LocalDate.of(2026, 1, 5), null));
        assertThrows(IllegalArgumentException.class, () -> acesso.revogar(LocalDate.of(2026, 1, 5), "  "));

        acesso.revogar(LocalDate.of(2026, 1, 5), "acesso indevido");
        assertFalse(acesso.ativo(LocalDate.of(2026, 1, 10)));
        assertThrows(IllegalStateException.class, () -> acesso.revogar(LocalDate.of(2026, 1, 6), "de novo"));
    }

    @Test
    @DisplayName("acesso ao processo exige processo, OAB, quem concedeu e data; expiracao nao pode ser antes da concessao")
    void acessoAoProcessoExigeCamposObrigatorios() {
        LocalDate hoje = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> new AcessoAoProcesso(null, "PE1", "PE2", hoje, null));
        assertThrows(IllegalArgumentException.class, () -> new AcessoAoProcesso(processo, null, "PE2", hoje, null));
        assertThrows(IllegalArgumentException.class, () -> new AcessoAoProcesso(processo, "PE1", null, hoje, null));
        assertThrows(IllegalArgumentException.class, () -> new AcessoAoProcesso(processo, "PE1", "PE2", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AcessoAoProcesso(processo, "PE1", "PE2", hoje, hoje.minusDays(1)));
    }

    // --- Regra do ultimo acesso: processo sigiloso nunca fica sem nenhum ativo ---

    @Test
    @DisplayName("nao revoga o unico acesso ativo do processo")
    void naoRevogaOUltimoAcessoAtivo() {
        AcessoAoProcesso unico = comId(1L, new AcessoAoProcesso(processo, "PE99999", "PE12345", LocalDate.of(2026, 1, 1), null));
        List<AcessoAoProcesso> todos = List.of(unico);

        assertFalse(AcessoAoProcesso.podeRevogar(todos, unico.getId(), LocalDate.now()));
    }

    @Test
    @DisplayName("revoga um acesso quando sobra outro ativo depois dele")
    void revogaQuandoSobraOutroAtivo() {
        AcessoAoProcesso primeiro = comId(1L, new AcessoAoProcesso(processo, "PE11111", "PE12345", LocalDate.of(2026, 1, 1), null));
        AcessoAoProcesso segundo = comId(2L, new AcessoAoProcesso(processo, "PE22222", "PE12345", LocalDate.of(2026, 1, 1), null));
        List<AcessoAoProcesso> todos = List.of(primeiro, segundo);

        assertTrue(AcessoAoProcesso.podeRevogar(todos, primeiro.getId(), LocalDate.now()));
    }

    @Test
    @DisplayName("nao revoga se o unico outro acesso ja estiver expirado")
    void naoRevogaSeOOutroJaExpirou() {
        AcessoAoProcesso ativo = comId(1L, new AcessoAoProcesso(processo, "PE11111", "PE12345", LocalDate.of(2026, 1, 1), null));
        AcessoAoProcesso expirado = comId(2L, new AcessoAoProcesso(processo, "PE22222", "PE12345",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)));
        List<AcessoAoProcesso> todos = List.of(ativo, expirado);

        assertFalse(AcessoAoProcesso.podeRevogar(todos, ativo.getId(), LocalDate.of(2026, 2, 1)));
    }

    // --- Maquina de estados da solicitacao ---

    @Test
    @DisplayName("solicitacao exige justificativa com pelo menos 20 caracteres")
    void solicitacaoExigeJustificativaMinima() {
        assertThrows(IllegalArgumentException.class,
                () -> new SolicitacaoAcesso(processo, "PE99999", "muito curta", LocalDate.now()));

        SolicitacaoAcesso valida = new SolicitacaoAcesso(processo, "PE99999",
                "preciso acompanhar a audiencia de conciliacao", LocalDate.now());
        assertEquals(StatusSolicitacao.PENDENTE, valida.getStatus());
    }

    @Test
    @DisplayName("aprovar muda para APROVADA e registra quem decidiu e quando")
    void aprovarMudaParaAprovada() {
        SolicitacaoAcesso solicitacao = novaSolicitacao();

        solicitacao.aprovar("PE00001", LocalDate.of(2026, 3, 1));

        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertEquals("PE00001", solicitacao.getDecididoPorOab());
        assertEquals(LocalDate.of(2026, 3, 1), solicitacao.getDecididoEm());
    }

    @Test
    @DisplayName("negar exige motivo e muda para NEGADA")
    void negarExigeMotivo() {
        SolicitacaoAcesso solicitacao = novaSolicitacao();

        assertThrows(IllegalArgumentException.class, () -> solicitacao.negar("PE00001", null, LocalDate.now()));

        solicitacao.negar("PE00001", "sem relação com o processo", LocalDate.of(2026, 3, 1));

        assertEquals(StatusSolicitacao.NEGADA, solicitacao.getStatus());
        assertEquals("sem relação com o processo", solicitacao.getMotivoNegacao());
    }

    @Test
    @DisplayName("cancelar muda para CANCELADA")
    void cancelarMudaParaCancelada() {
        SolicitacaoAcesso solicitacao = novaSolicitacao();

        solicitacao.cancelar(LocalDate.of(2026, 3, 1));

        assertEquals(StatusSolicitacao.CANCELADA, solicitacao.getStatus());
    }

    @Test
    @DisplayName("transicao invalida e recusada: nao aprova, nega ou cancela solicitacao ja decidida")
    void transicaoInvalidaERecusada() {
        SolicitacaoAcesso aprovada = novaSolicitacao();
        aprovada.aprovar("PE00001", LocalDate.of(2026, 3, 1));

        assertThrows(IllegalStateException.class, () -> aprovada.aprovar("PE00001", LocalDate.now()));
        assertThrows(IllegalStateException.class, () -> aprovada.negar("PE00001", "motivo", LocalDate.now()));
        assertThrows(IllegalStateException.class, () -> aprovada.cancelar(LocalDate.now()));

        SolicitacaoAcesso cancelada = novaSolicitacao();
        cancelada.cancelar(LocalDate.now());
        assertThrows(IllegalStateException.class, () -> cancelada.aprovar("PE00001", LocalDate.now()));
    }

    @Test
    @DisplayName("ja existe solicitacao pendente da mesma OAB para o mesmo processo")
    void solicitacaoDuplicadaDaMesmaOab() {
        SolicitacaoAcesso pendente = novaSolicitacao();
        List<SolicitacaoAcesso> doProcesso = List.of(pendente);

        assertTrue(SolicitacaoAcesso.existePendenteDaMesmaOab(doProcesso, "PE99999"));
        assertFalse(SolicitacaoAcesso.existePendenteDaMesmaOab(doProcesso, "PE88888"));

        pendente.cancelar(LocalDate.now());
        assertFalse(SolicitacaoAcesso.existePendenteDaMesmaOab(doProcesso, "PE99999"),
                "cancelada nao conta mais como pendente");
    }

    // --- Proxy registra auditoria em toda tentativa de leitura ---

    @Test
    @DisplayName("Proxy registra auditoria PERMITIDO quando a leitura e aceita")
    void proxyRegistraAuditoriaPermitido() {
        ArquivoAnexo publico = anexo(false, Set.of());
        AcessoRestrito<ArquivoAnexo> real = id -> publico;
        List<EventoDeAuditoria> registrados = new ArrayList<>();

        ArquivoProxy proxy = new ArquivoProxy(real, Solicitante.deOab("PE99999"),
                semAcesso(), registrados::add, LocalDate.now());
        proxy.carregar(7L);

        assertEquals(1, registrados.size());
        EventoDeAuditoria evento = registrados.get(0);
        assertEquals(ResultadoAcesso.PERMITIDO, evento.resultado());
        assertEquals("PE99999", evento.oab());
        assertEquals(7L, evento.conteudoId());
        assertEquals("arquivo", evento.conteudoTipo());
        assertEquals(processo, evento.numeroProcesso());
    }

    @Test
    @DisplayName("Proxy registra auditoria NEGADO quando a leitura e barrada")
    void proxyRegistraAuditoriaNegado() {
        ArquivoAnexo sigiloso = anexo(true, Set.of("PE12345"));
        AcessoRestrito<ArquivoAnexo> real = id -> sigiloso;
        List<EventoDeAuditoria> registrados = new ArrayList<>();

        ArquivoProxy proxy = new ArquivoProxy(real, Solicitante.deOab("PE99999"),
                semAcesso(), registrados::add, LocalDate.now());

        assertThrows(ProxyDeAcesso.AcessoNegadoException.class, () -> proxy.carregar(8L));

        assertEquals(1, registrados.size());
        EventoDeAuditoria evento = registrados.get(0);
        assertEquals(ResultadoAcesso.NEGADO, evento.resultado());
        assertNotNull(evento.motivo());
    }

    // --- Apoio ---

    private ArquivoAnexo anexo(boolean segredo, Set<String> oabsHabilitadas) {
        return new ArquivoAnexo(1L, processo, "laudo.pdf", TipoArquivo.PDF,
                "conteúdo".getBytes(), null, LocalDate.of(2026, 1, 1), "PE12345", segredo, oabsHabilitadas);
    }

    private static VerificadorDeAcessoAoProcesso semAcesso() {
        return (proc, oab, hoje) -> false;
    }

    private SolicitacaoAcesso novaSolicitacao() {
        return new SolicitacaoAcesso(processo, "PE99999",
                "preciso acompanhar a audiencia de conciliacao", LocalDate.of(2026, 2, 1));
    }

    /** Reflection seria exagero: os testes de regra do ultimo acesso soh precisam de um id estavel. */
    private static AcessoAoProcesso comId(Long id, AcessoAoProcesso semId) {
        return new AcessoAoProcesso(id, semId.getNumeroProcesso(), semId.getOab(), semId.getConcedidoPor(),
                semId.getConcedidoEm(), semId.getExpiraEm(), semId.getRevogadoEm(), semId.getMotivoRevogacao());
    }
}
