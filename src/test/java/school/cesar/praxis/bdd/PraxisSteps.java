package school.cesar.praxis.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.documento.DocumentoProxy;
import school.cesar.praxis.domain.documento.TipoDocumento;
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
    private NotificadorPainel painel;

    @Autowired
    private ProcessoJpaRepository processosJpa;
    @Autowired
    private PrazoJpaRepository prazosJpa;
    @Autowired
    private DocumentoJpaRepository documentosJpa;
    @Autowired
    private NotificacaoJpaRepository notificacoesJpa;
    @Autowired
    private DocumentosUseCases.EnviarDocumentoParaRevisao enviarParaRevisao;
    @Autowired
    private DocumentosUseCases.AprovarDocumento aprovarDocumento;
    @Autowired
    private DocumentosUseCases.RejeitarDocumento rejeitarDocumento;
    @Autowired
    private DocumentosUseCases.DesfazerDecisaoDocumento desfazerDecisao;
    @Autowired
    private DocumentosUseCases.ProtocolarDocumento protocolarDocumento;

    // Estado do cenario
    private String numeroProcesso;
    private String cliente;
    private String comarca;
    private boolean segredoJustica;
    private String oabResponsavel;
    private Prazo prazo;
    private DocumentoGerado documento;
    private final List<AlertaPrazo> alertas = new ArrayList<>();

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

        // --- Fluxo de aprovacao de documentos ---

    @Quando("eu envio o documento para revisao")
    public void euEnvioODocumentoParaRevisao() {
        documento = enviarParaRevisao.executar(
                new DocumentosUseCases.EnviarDocumentoParaRevisao.Comando(documento.getId()));
    }

    @E("eu aprovo o documento com OAB {string} e comentario {string}")
    public void euAprovoODocumento(String oab, String comentario) {
        documento = aprovarDocumento.executar(
                new DocumentosUseCases.AprovarDocumento.Comando(documento.getId(), oab, comentario));
    }

    @E("eu rejeito o documento com OAB {string} e motivo {string}")
    public void euRejeitoODocumento(String oab, String motivo) {
        documento = rejeitarDocumento.executar(
                new DocumentosUseCases.RejeitarDocumento.Comando(documento.getId(), oab, motivo));
    }

    @E("eu protocolo o documento")
    public void euProtocoloODocumento() {
        documento = protocolarDocumento.executar(
                new DocumentosUseCases.ProtocolarDocumento.Comando(documento.getId()));
    }

    @E("eu desfaco a ultima decisao do documento")
    public void euDesfacoAUltimaDecisao() {
        documento = desfazerDecisao.executar(
                new DocumentosUseCases.DesfazerDecisaoDocumento.Comando(documento.getId()));
    }

    @Entao("o status do documento deve ser {string}")
    public void oStatusDoDocumentoDeveSer(String statusEsperado) {
        assertEquals(statusEsperado, documento.getStatus().nome());
    }

    @E("o historico do documento deve conter uma transicao de {string} para {string}")
    public void oHistoricoDeveConterTransicao(String de, String para) {
        assertTrue(documento.getHistorico().stream()
                        .anyMatch(registro -> registro.getDeEstado().equals(de)
                                && registro.getParaEstado().equals(para)),
                "historico nao contem transicao de " + de + " para " + para);
    }

    @Entao("tentar aprovar o documento deve falhar")
    public void tentarAprovarDeveFalhar() {
        assertThrows(IllegalStateException.class, () -> aprovarDocumento.executar(
                new DocumentosUseCases.AprovarDocumento.Comando(documento.getId(), "PE12345", "x")));
    }
}
