package school.cesar.praxis.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.DocumentoProxy;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.infrastructure.persistence.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automacao dos cenarios BDD. Os passos exercitam os casos de uso reais da
 * aplicacao (com banco relacional), nao dublês do dominio.
 */
public class PraxisSteps {

    @Autowired
    private ProcessosUseCases.CadastrarProcesso cadastrarProcesso;
    @Autowired
    private PrazosUseCases.AbrirPrazo abrirPrazo;
    @Autowired
    private PrazosUseCases.CumprirPrazo cumprirPrazo;
    @Autowired
    private PrazosUseCases.VarrerPrazos varrerPrazos;
    @Autowired
    private DocumentosUseCases.GerarDocumento gerarDocumento;
    @Autowired
    private DocumentosUseCases.BaixarDocumento baixarDocumento;
    @Autowired
    private DocumentosUseCases.ListarDocumentos listarDocumentos;
    @Autowired
    private FeriadosUseCases.CadastrarFeriado cadastrarFeriado;
    @Autowired
    private FeriadosUseCases.RemoverFeriado removerFeriado;
    @Autowired
    private FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil;
    @Autowired
    private NotificadorPainel painel;

    @Autowired
    private ProcessoJpaRepository processosJpa;
    @Autowired
    private PrazoJpaRepository prazosJpa;
    @Autowired
    private DocumentoJpaRepository documentosJpa;
    @Autowired
    private NotificacaoJpaRepository notificacoesJpa;

    // Estado do cenario
    private String numeroProcesso;
    private String cliente;
    private String comarca;
    private boolean segredoJustica;
    private String oabResponsavel;
    private Prazo prazo;
    private DocumentoGerado documento;
    private final List<AlertaPrazo> alertas = new ArrayList<>();
    private final List<Long> feriadosDoCenario = new ArrayList<>();

    @Before
    public void limparEstado() {
        prazosJpa.deleteAll();
        documentosJpa.deleteAll();
        notificacoesJpa.deleteAll();
        processosJpa.deleteAll();
        painel.limpar();
        alertas.clear();
        prazo = null;
        documento = null;
        segredoJustica = false;
    }

    /**
     * Remove apenas os feriados criados pelo cenario - a carga de referencia
     * (feriados nacionais) tem de sobreviver, porque os cenarios de prazo
     * dependem dela. A remocao passa pelo caso de uso, e nao pelo repositorio
     * JPA, para que o cache do adaptador seja invalidado.
     */
    @After
    public void removerFeriadosDoCenario() {
        feriadosDoCenario.forEach(removerFeriado::executar);
        feriadosDoCenario.clear();
    }

    // --- Contexto ---

    @Dado("um processo {string} do cliente {string} na comarca de {string}")
    public void umProcesso(String numero, String nomeCliente, String nomeComarca) {
        this.numeroProcesso = numero;
        this.cliente = nomeCliente;
        this.comarca = nomeComarca;
        this.segredoJustica = false;
    }

    @Dado("um processo em segredo de justica {string} do cliente {string} na comarca de {string}")
    public void umProcessoEmSegredo(String numero, String nomeCliente, String nomeComarca) {
        this.numeroProcesso = numero;
        this.cliente = nomeCliente;
        this.comarca = nomeComarca;
        this.segredoJustica = true;
    }

    @E("o advogado responsavel {string} com e-mail {string} e OAB {string}")
    public void oAdvogadoResponsavel(String nome, String email, String oab) {
        this.oabResponsavel = oab;
        cadastrarProcesso.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                numeroProcesso, cliente, comarca, segredoJustica, nome, email, oab));
    }

    // --- Motor de prazos ---

    @Dado("um prazo fatal {string} de {int} dias uteis intimado em {string}")
    public void umPrazoFatal(String descricao, int dias, String intimacao) {
        prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                numeroProcesso, descricao, LocalDate.parse(intimacao), dias, true,
                RegimeContagem.DIAS_UTEIS));
    }

    @Dado("um prazo comum {string} de {int} dias uteis intimado em {string}")
    public void umPrazoComum(String descricao, int dias, String intimacao) {
        prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                numeroProcesso, descricao, LocalDate.parse(intimacao), dias, false,
                RegimeContagem.DIAS_UTEIS));
    }

    @Quando("a varredura de prazos roda em {string}")
    public void aVarreduraRodaEm(String dia) {
        alertas.addAll(varrerPrazos.executar(LocalDate.parse(dia)));
    }

    @Quando("o prazo e cumprido")
    public void oPrazoECumprido() {
        cumprirPrazo.executar(prazo.getId());
    }

    @Entao("o vencimento do prazo deve ser {string}")
    public void oVencimentoDeveSer(String vencimento) {
        assertEquals(LocalDate.parse(vencimento), prazo.getVencimento());
    }

    @Entao("o advogado {string} deve ser notificado com nivel {string}")
    public void deveSerNotificadoComNivel(String email, String nivel) {
        assertFalse(alertas.isEmpty(), "nenhum alerta emitido");
        AlertaPrazo alerta = alertas.get(alertas.size() - 1);
        assertEquals(nivel, alerta.nivel().name());
        assertEquals(email, alerta.destinatario().email());

        assertTrue(painel.getEntregues().stream()
                        .anyMatch(notificacao -> notificacao.destinatario().equals(email)
                                && notificacao.assunto().contains(nivel)),
                "painel nao recebeu notificacao de nivel " + nivel);
    }

    @E("a notificacao deve informar {int} dias restantes")
    public void deveInformarDiasRestantes(int dias) {
        AlertaPrazo alerta = alertas.get(alertas.size() - 1);
        assertEquals(dias, alerta.diasRestantes());
        assertTrue(painel.getEntregues().get(painel.getEntregues().size() - 1)
                .assunto().contains(dias + " dia(s)"));
    }

    @Entao("o advogado deve receber exatamente {int} notificacao")
    public void deveReceberExatamente(int quantidade) {
        assertEquals(quantidade, painel.getEntregues().size());
        assertEquals(quantidade, alertas.size());
    }

    @Entao("os niveis de alerta emitidos devem ser {string}")
    public void osNiveisEmitidosDevemSer(String esperados) {
        List<String> lista = alertas.stream().map(alerta -> alerta.nivel().name()).toList();
        assertEquals(esperados, String.join(", ", lista));
    }

    @Entao("nenhuma notificacao deve ser enviada")
    public void nenhumaNotificacao() {
        assertTrue(alertas.isEmpty(), "alertas inesperados: " + alertas);
        assertTrue(painel.getEntregues().isEmpty(), "painel recebeu notificacao inesperada");
    }

    @Entao("o advogado {string} deve ser notificado sobre prazo vencido")
    public void notificadoSobrePrazoVencido(String email) {
        assertFalse(alertas.isEmpty(), "nenhum alerta emitido");
        assertTrue(alertas.get(alertas.size() - 1).vencido());
        assertTrue(painel.getEntregues().stream()
                .anyMatch(notificacao -> notificacao.destinatario().equals(email)
                        && notificacao.assunto().contains("VENCIDO")));
    }

    // --- Cadastro de feriados ---

    @Dado("o feriado {string} cadastrado em {string} valido em todo o pais")
    public void oFeriadoNacional(String descricao, String data) {
        cadastrar(descricao, data, false, Abrangencia.Nivel.NACIONAL, null);
    }

    @Quando("o feriado {string} e cadastrado em {string} valido em todo o pais")
    public void oFeriadoNacionalECadastrado(String descricao, String data) {
        cadastrar(descricao, data, false, Abrangencia.Nivel.NACIONAL, null);
    }

    @Dado("o feriado {string} cadastrado em {string} repetindo todo ano")
    public void oFeriadoAnual(String descricao, String data) {
        cadastrar(descricao, data, true, Abrangencia.Nivel.NACIONAL, null);
    }

    @Dado("o feriado {string} cadastrado em {string} so na comarca de {string}")
    public void oFeriadoDaComarca(String descricao, String data, String comarcaDoFeriado) {
        cadastrar(descricao, data, false, Abrangencia.Nivel.COMARCAL, comarcaDoFeriado);
    }

    @Quando("eu removo o feriado cadastrado")
    public void euRemovoOFeriado() {
        removerFeriado.executar(feriadosDoCenario.remove(feriadosDoCenario.size() - 1));
    }

    @Entao("o dia {string} nao deve correr prazo")
    public void oDiaNaoDeveCorrerPrazo(String data) {
        assertFalse(consultarDiaUtil.executar(LocalDate.parse(data)).diaUtil(),
                data + " deveria estar sem expediente forense");
    }

    @Entao("o dia {string} deve correr prazo")
    public void oDiaDeveCorrerPrazo(String data) {
        assertTrue(consultarDiaUtil.executar(LocalDate.parse(data)).diaUtil(),
                data + " deveria ser dia util");
    }

    @E("o proximo dia util depois de {string} deve ser {string}")
    public void oProximoDiaUtilDeveSer(String data, String esperado) {
        assertEquals(LocalDate.parse(esperado),
                consultarDiaUtil.executar(LocalDate.parse(data)).proximoDiaUtil());
    }

    @Entao("o vencimento do prazo ja lancado deve continuar {string}")
    public void oVencimentoJaLancadoDeveContinuar(String vencimento) {
        // Le do banco de novo: o vencimento ficou congelado na abertura.
        LocalDate persistido = prazosJpa.findById(prazo.getId()).orElseThrow().getVencimento();
        assertEquals(LocalDate.parse(vencimento), persistido);
    }

    @E("um novo prazo fatal de {int} dias uteis intimado em {string} deve vencer em {string}")
    public void umNovoPrazoDeveVencerEm(int dias, String intimacao, String vencimento) {
        Prazo novo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                numeroProcesso, "Prazo aberto depois do feriado", LocalDate.parse(intimacao),
                dias, true, RegimeContagem.DIAS_UTEIS));

        assertEquals(LocalDate.parse(vencimento), novo.getVencimento());
    }

    private void cadastrar(String descricao,
                           String data,
                           boolean repeteTodoAno,
                           Abrangencia.Nivel nivel,
                           String abrangencia) {
        FeriadosUseCases.ItemFeriado item = cadastrarFeriado.executar(
                new FeriadosUseCases.CadastrarFeriado.Comando(
                        descricao, LocalDate.parse(data), repeteTodoAno, nivel, abrangencia));
        feriadosDoCenario.add(item.id());
    }

    // --- Geracao de documentos ---

    @Quando("eu gero a peca {string} com os campos:")
    public void euGeroAPeca(String tipo, DataTable tabela) {
        Map<String, String> campos = tabela.asMap(String.class, String.class);
        documento = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numeroProcesso, TipoDocumento.valueOf(tipo), campos, oabResponsavel));
    }

    @Entao("a peca gerada deve conter {string}")
    public void aPecaDeveConter(String trecho) {
        assertTrue(documento.getConteudo().contains(trecho),
                "peca nao contem \"" + trecho + "\":\n" + documento.getConteudo());
    }

    @E("a peca gerada nao deve conter {string}")
    public void aPecaNaoDeveConter(String trecho) {
        assertFalse(documento.getConteudo().contains(trecho));
    }

    @E("a peca deve ficar registrada nos autos do processo")
    public void aPecaDeveFicarRegistrada() {
        List<DocumentosUseCases.ListarDocumentos.ItemDocumento> itens =
                listarDocumentos.executar(numeroProcesso);
        assertTrue(itens.stream().anyMatch(item -> item.id().equals(documento.getId())));
    }

    @Entao("o advogado {string} deve ser notificado sobre documento gerado")
    public void notificadoSobreDocumento(String email) {
        List<Notificacao> entregues = painel.getEntregues();
        assertTrue(entregues.stream()
                        .anyMatch(notificacao -> notificacao.destinatario().equals(email)
                                && notificacao.assunto().startsWith("Documento gerado")),
                "painel nao recebeu aviso de documento gerado: " + entregues);
    }

    @Entao("a leitura da peca pela OAB {string} deve ser permitida")
    public void leituraPermitida(String oab) {
        DocumentoGerado lido = baixarDocumento.executar(documento.getId(), oab);
        assertEquals(documento.getId(), lido.getId());
        assertFalse(lido.getConteudo().isBlank());
    }

    @E("a leitura da peca pela OAB {string} deve ser negada")
    public void leituraNegada(String oab) {
        assertThrows(DocumentoProxy.AcessoNegadoException.class,
                () -> baixarDocumento.executar(documento.getId(), oab));
    }
}
