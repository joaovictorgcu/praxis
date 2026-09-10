package school.cesar.praxis.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Mas;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import school.cesar.praxis.application.port.in.DocumentosUseCases;
import school.cesar.praxis.application.port.in.AnexosUseCases;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.application.port.in.HonorariosUseCases;
import school.cesar.praxis.application.port.in.ModelosUseCases;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.documento.DocumentoGerado;
import school.cesar.praxis.domain.anexo.ArquivoAnexo;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;
import school.cesar.praxis.domain.documento.TipoDocumento;
import school.cesar.praxis.domain.feriado.Abrangencia;
import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;
import school.cesar.praxis.infrastructure.notificacao.NotificadorPainel;
import school.cesar.praxis.infrastructure.persistence.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    private AnexosUseCases.AnexarArquivo anexarArquivo;
    @Autowired
    private AnexosUseCases.ListarAnexos listarAnexos;
    @Autowired
    private AnexosUseCases.BaixarAnexo baixarAnexo;
    @Autowired
    private ModelosUseCases.CadastrarModelo cadastrarModelo;
    @Autowired
    private ModelosUseCases.RemoverModelo removerModelo;
    @Autowired
    private FeriadosUseCases.CadastrarFeriado cadastrarFeriado;
    @Autowired
    private FeriadosUseCases.RemoverFeriado removerFeriado;
    @Autowired
    private FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil;
    @Autowired
    private HonorariosUseCases.CadastrarContrato cadastrarContrato;
    @Autowired
    private HonorariosUseCases.ListarContratos listarContratos;
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
    private ContratoHonorarioJpaRepository contratosJpa;
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
    private final List<Long> feriadosDoCenario = new ArrayList<>();
    private final List<Long> modelosDoCenario = new ArrayList<>();
    private ModelosUseCases.ItemModelo modelo;
    private AnexosUseCases.ItemAnexo anexo;
    private HonorariosUseCases.ItemContrato contrato;
    private RuntimeException falhaEsperada;

    @Before
    public void limparEstado() {
        prazosJpa.deleteAll();
        documentosJpa.deleteAll();
        notificacoesJpa.deleteAll();
        contratosJpa.deleteAll();
        processosJpa.deleteAll();
        painel.limpar();
        alertas.clear();
        prazo = null;
        documento = null;
        modelo = null;
        anexo = null;
        contrato = null;
        falhaEsperada = null;
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

    /** Mesma regra dos feriados: os modelos de exemplo tem de sobreviver. */
    @After
    public void removerModelosDoCenario() {
        modelosDoCenario.forEach(removerModelo::executar);
        modelosDoCenario.clear();
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

    // --- Cadastro de modelos de documento ---

    @Dado("o modelo cadastrado:")
    public void oModeloCadastrado(DataTable tabela) {
        Map<String, String> dados = tabela.asMap(String.class, String.class);
        modelo = cadastrarModelo.executar(new ModelosUseCases.CadastrarModelo.Comando(
                dados.get("codigo"),
                dados.get("nome"),
                TipoDocumento.valueOf(dados.get("tipo")),
                dados.get("titulo"),
                dados.get("corpo"),
                dados.get("pedidos"),
                "sim".equals(dados.get("juizo"))));
        modelosDoCenario.add(modelo.id());
    }

    @Quando("eu gero a peca pelo modelo {string} com os campos:")
    public void euGeroPeloModelo(String codigo, DataTable tabela) {
        gerarPeloModelo(codigo, tabela.asMap(String.class, String.class));
    }

    @Quando("eu gero a peca pelo modelo {string} sem informar campos")
    public void euGeroPeloModeloSemCampos(String codigo) {
        gerarPeloModelo(codigo, Map.of());
    }

    @Quando("eu removo o modelo cadastrado")
    public void euRemovoOModelo() {
        removerModelo.executar(modelosDoCenario.remove(modelosDoCenario.size() - 1));
    }

    @Quando("eu tento cadastrar outro modelo com o codigo {string}")
    public void euTentoCadastrarComCodigoRepetido(String codigo) {
        falhaEsperada = assertThrows(IllegalArgumentException.class,
                () -> cadastrarModelo.executar(new ModelosUseCases.CadastrarModelo.Comando(
                        codigo, "Segundo modelo", TipoDocumento.PETICAO_INICIAL, null,
                        "Outro corpo.", "Outros pedidos.", true)));
    }

    @Quando("eu tento gerar a peca {string} sem modelo")
    public void euTentoGerarSemModelo(String tipo) {
        falhaEsperada = assertThrows(IllegalArgumentException.class,
                () -> gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                        numeroProcesso, TipoDocumento.valueOf(tipo), Map.of(), oabResponsavel)));
    }

    @E("eu tento gerar a peca pelo modelo {string}")
    public void euTentoGerarPeloModelo(String codigo) {
        falhaEsperada = assertThrows(NoSuchElementException.class,
                () -> gerarPeloModelo(codigo, Map.of()));
    }

    @Entao("o tipo da peca gerada deve ser {string}")
    public void oTipoDaPecaDeveSer(String tipo) {
        assertEquals(TipoDocumento.valueOf(tipo), documento.getTipo());
    }

    @Entao("o cadastro do modelo deve ser recusado")
    public void oCadastroDoModeloDeveSerRecusado() {
        assertNotNull(falhaEsperada, "o cadastro duplicado foi aceito");
        assertTrue(falhaEsperada.getMessage().contains("ja existe modelo"),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    @Entao("a geracao deve ser recusada por falta de gerador")
    public void recusadaPorFaltaDeGerador() {
        assertNotNull(falhaEsperada, "a geracao foi aceita");
        assertTrue(falhaEsperada.getMessage().contains("nao tem gerador compilado"),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    @Entao("a geracao deve ser recusada por modelo inexistente")
    public void recusadaPorModeloInexistente() {
        assertNotNull(falhaEsperada, "a geracao foi aceita");
        assertTrue(falhaEsperada.getMessage().contains("modelo nao encontrado"),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    private void gerarPeloModelo(String codigo, Map<String, String> campos) {
        documento = gerarDocumento.executar(new DocumentosUseCases.GerarDocumento.Comando(
                numeroProcesso, TipoDocumento.PECA_AVULSA, campos, oabResponsavel, codigo));
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
        assertThrows(ProxyDeAcesso.AcessoNegadoException.class,
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

    // --- Anexacao de arquivos ao processo ---

    @Quando("eu junto aos autos o arquivo {string} do tipo {string}")
    public void euJuntoOArquivo(String nome, String mime) {
        anexo = juntar(numeroProcesso, nome, mime, "conteudo de teste".getBytes());
    }

    @Quando("eu tento juntar aos autos o arquivo {string} do tipo {string}")
    public void euTentoJuntarTipoInvalido(String nome, String mime) {
        falhaEsperada = assertThrows(IllegalArgumentException.class,
                () -> juntar(numeroProcesso, nome, mime, "conteudo de teste".getBytes()));
    }

    @Quando("eu tento juntar aos autos um arquivo vazio")
    public void euTentoJuntarArquivoVazio() {
        falhaEsperada = assertThrows(IllegalArgumentException.class,
                () -> juntar(numeroProcesso, "vazio.pdf", "application/pdf", new byte[0]));
    }

    @Quando("eu tento juntar o arquivo {string} ao processo {string}")
    public void euTentoJuntarEmProcessoInexistente(String nome, String numero) {
        falhaEsperada = assertThrows(NoSuchElementException.class,
                () -> juntar(numero, nome, "application/pdf", "conteudo".getBytes()));
    }

    @Entao("o anexo deve constar na lista de arquivos do processo")
    public void oAnexoDeveConstarNaLista() {
        assertTrue(listarAnexos.executar(numeroProcesso).stream()
                        .anyMatch(item -> item.id().equals(anexo.id())),
                "anexo nao aparece na lista do processo");
    }

    @Entao("o nome do anexo deve ser {string}")
    public void oNomeDoAnexoDeveSer(String nome) {
        assertEquals(nome, anexo.nome());
    }

    @Entao("o anexo deve poder ser lido pela OAB {string}")
    public void anexoLidoPor(String oab) {
        ArquivoAnexo lido = baixarAnexo.executar(anexo.id(), oab);
        assertEquals(anexo.id(), lido.getId());
        assertTrue(lido.tamanhoBytes() > 0);
    }

    @Mas("a leitura do anexo pela OAB {string} deve ser negada")
    public void leituraDoAnexoNegada(String oab) {
        assertThrows(ProxyDeAcesso.AcessoNegadoException.class,
                () -> baixarAnexo.executar(anexo.id(), oab));
    }

    @E("a leitura do anexo sem OAB deve ser negada")
    public void leituraDoAnexoSemOab() {
        assertThrows(ProxyDeAcesso.AcessoNegadoException.class,
                () -> baixarAnexo.executar(anexo.id(), "  "));
    }

    @E("o advogado responsavel deve ser notificado sobre o arquivo juntado")
    public void notificadoSobreAnexo() {
        List<Notificacao> entregues = painel.getEntregues();
        assertTrue(entregues.stream()
                        .anyMatch(notificacao -> notificacao.assunto().startsWith("Arquivo juntado")),
                "painel nao recebeu aviso de arquivo juntado: " + entregues);
    }

    @Entao("a juntada deve ser recusada por tipo nao aceito")
    public void recusadaPorTipo() {
        assertRecusa("tipo de arquivo nao aceito");
    }

    @Entao("a juntada deve ser recusada por falta de conteudo")
    public void recusadaPorConteudo() {
        assertRecusa("anexo sem conteudo");
    }

    @Entao("a juntada deve ser recusada por processo inexistente")
    public void recusadaPorProcesso() {
        assertRecusa("processo nao encontrado");
    }

    private AnexosUseCases.ItemAnexo juntar(String numero, String nome, String mime, byte[] bytes) {
        return anexarArquivo.executar(new AnexosUseCases.AnexarArquivo.Comando(
                numero, nome, mime, bytes, "arquivo de teste", oabResponsavel));
    }

    private void assertRecusa(String motivoEsperado) {
        assertNotNull(falhaEsperada, "a operacao foi aceita");
        assertTrue(falhaEsperada.getMessage().contains(motivoEsperado),
                "motivo inesperado: " + falhaEsperada.getMessage());
    }

    @Quando("eu contrato honorario fixo de {string}")
    public void euContratoHonorarioFixo(String valor) {
        contrato = contratarFixo(numeroProcesso, valor);
    }

    @Dado("que eu contratei honorario fixo de {string}")
    public void queEuContrateiHonorarioFixo(String valor) {
        contrato = contratarFixo(numeroProcesso, valor);
    }

    @Quando("eu tento contratar honorario fixo de {string} no processo {string}")
    public void euTentoContratarHonorarioFixoNoProcesso(String valor, String numero) {
        falhaEsperada = assertThrows(NoSuchElementException.class,
                () -> contratarFixo(numero, valor));
    }

    @Quando("eu contrato honorario por hora de {string} com {int} horas trabalhadas")
    public void euContratoHonorarioPorHora(String valorHora, int horas) {
        contrato = cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                numeroProcesso, "POR_HORA", LocalDate.now(), null, new BigDecimal(valorHora), horas,
                null, null));
    }

    @Quando("eu contrato honorario quota litis de {int}% sobre causa de {string}")
    public void euContratoHonorarioQuotaLitis(int percentual, String valorCausa) {
        contrato = cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                numeroProcesso, "QUOTA_LITIS", LocalDate.now(), null, null, 0,
                new BigDecimal(valorCausa), BigDecimal.valueOf(percentual)));
    }

    @Quando("eu tento contratar honorario quota litis de {int}% sobre causa de {string}")
    public void euTentoContratarHonorarioQuotaLitis(int percentual, String valorCausa) {
        falhaEsperada = assertThrows(IllegalArgumentException.class,
                () -> cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                        numeroProcesso, "QUOTA_LITIS", LocalDate.now(), null, null, 0,
                        new BigDecimal(valorCausa), BigDecimal.valueOf(percentual))));
    }

    @Entao("o valor contratado deve ser {string}")
    public void oValorContratadoDeveSer(String valor) {
        assertEquals(new BigDecimal(valor), contrato.valorContratado());
    }

    @E("a modalidade do contrato deve ser {string}")
    public void aModalidadeDoContratoDeveSer(String modalidade) {
        assertEquals(modalidade, contrato.modalidade());
    }

    @Entao("o contrato deve constar na lista de honorarios do processo")
    public void oContratoDeveConstarNaLista() {
        assertTrue(listarContratos.executar(numeroProcesso).stream()
                        .anyMatch(item -> item.id().equals(contrato.id())),
                "contrato nao aparece na lista do processo");
    }

    @Entao("a contratacao deve ser recusada por limite etico")
    public void aContratacaoDeveSerRecusadaPorLimiteEtico() {
        assertRecusa("limite etico");
    }

    @Entao("a contratacao deve ser recusada por processo inexistente")
    public void aContratacaoDeveSerRecusadaPorProcessoInexistente() {
        assertRecusa("processo nao encontrado");
    }

    private HonorariosUseCases.ItemContrato contratarFixo(String numero, String valor) {
        return cadastrarContrato.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                numero, "FIXO", LocalDate.now(), new BigDecimal(valor), null, 0, null, null));
    }
}