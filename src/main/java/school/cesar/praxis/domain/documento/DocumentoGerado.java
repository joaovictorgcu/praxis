package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.compartilhado.ConteudoRestrito;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Documentos</b>: a peca ja
 * materializada e anexada aos autos.
 *
 * <p>Carrega consigo a restricao de acesso (segredo de justica e OABs habilitadas)
 * porque quem decide se um documento pode ser lido e o proprio documento - o
 * {@link DocumentoProxy} apenas faz cumprir.
 *
 * <p>O texto e versionado: a geracao e a versao 1, e cada edicao, regeneracao
 * ou restauracao incrementa {@link #getVersaoAtual()}. O estado do fluxo de
 * aprovacao (State) decide se o texto ainda pode mudar. A peca guarda o modelo
 * e os campos usados na geracao, para poder ser regenerada depois.
 */
public class DocumentoGerado implements ConteudoRestrito {

    private static final String QUEBRA_WINDOWS = "\r\n";
    private static final String QUEBRA = "\n";

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final TipoDocumento tipo;
    private String conteudo;
    private final LocalDate geradoEm;
    private final String geradoPorOab;
    private final boolean segredoJustica;
    private final Set<String> oabsHabilitadas = new LinkedHashSet<>();

    private StatusDocumento status;
    private final List<RegistroAprovacao> historico = new ArrayList<>();
    /** Codigo do modelo cadastrado usado na geracao; nulo quando foi a peca compilada. */
    private final String modeloCodigo;
    /** Campos informados na geracao (ou na ultima regeneracao). */
    private final Map<String, String> camposGeracao = new LinkedHashMap<>();
    private int versaoAtual;

    public DocumentoGerado(Long id,
                           NumeroCnj numeroProcesso,
                           TipoDocumento tipo,
                           String conteudo,
                           LocalDate geradoEm,
                           String geradoPorOab,
                           boolean segredoJustica,
                           Set<String> oabsHabilitadas) {
        this(id, numeroProcesso, tipo, conteudo, geradoEm, geradoPorOab, segredoJustica, oabsHabilitadas,
                null, Map.of(), 1);
    }

    /** Peca com a origem da geracao (modelo e campos) e a versao atual do texto. */
    public DocumentoGerado(Long id,
                           NumeroCnj numeroProcesso,
                           TipoDocumento tipo,
                           String conteudo,
                           LocalDate geradoEm,
                           String geradoPorOab,
                           boolean segredoJustica,
                           Set<String> oabsHabilitadas,
                           String modeloCodigo,
                           Map<String, String> camposGeracao,
                           int versaoAtual) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("documento exige processo");
        }
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("documento sem conteúdo");
        }
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.geradoEm = geradoEm;
        this.geradoPorOab = geradoPorOab;
        this.segredoJustica = segredoJustica;
        if (oabsHabilitadas != null) {
            this.oabsHabilitadas.addAll(oabsHabilitadas);
        }
        if (versaoAtual < 1) {
            throw new IllegalArgumentException("a versão do texto começa em 1");
        }
        this.modeloCodigo = modeloCodigo == null || modeloCodigo.isBlank() ? null : modeloCodigo;
        if (camposGeracao != null) {
            this.camposGeracao.putAll(camposGeracao);
        }
        this.versaoAtual = versaoAtual;
        this.status = new Rascunho();
    }

    /** Art. 189 do CPC: em segredo de justica, so quem esta habilitado nos autos le. */
    @Override
    public boolean podeSerLidoPor(String oabSolicitante) {
        if (!segredoJustica) {
            return true;
        }
        return oabSolicitante != null && oabsHabilitadas.contains(oabSolicitante);
    }

    public String nomeArquivo() {
        return tipo.name().toLowerCase() + "-" + numeroProcesso.valor().replace('.', '-') + ".txt";
    }

    public void enviarParaRevisao() {
        transicionar(status.enviarParaRevisao(this), null, null);
    }

    public void aprovar(String oabAprovador, String comentario) {
        transicionar(status.aprovar(this, oabAprovador, comentario), oabAprovador, comentario);
    }

    public void rejeitar(String oabAprovador, String motivo) {
        transicionar(status.rejeitar(this, oabAprovador, motivo), oabAprovador, motivo);
    }

    public void protocolar() {
        transicionar(status.protocolar(this), null, null);
    }

    // --- Texto versionado ---

    /**
     * Edicao manual do texto. O estado decide se cabe (State); texto igual ao
     * atual nao vira versao nova. Quebras de linha do navegador (CRLF) sao
     * normalizadas, para a comparacao entre versoes nao acusar diferenca falsa.
     */
    public void editar(String novoConteudo) {
        substituirTexto(novoConteudo, "o texto não mudou: nada a salvar");
    }

    /** Texto refeito pelo gerador com campos novos; os campos passam a ser os da peca. */
    public void regenerar(String novoConteudo, Map<String, String> novosCampos) {
        substituirTexto(novoConteudo, "a regeneração produziu o mesmo texto: nada a salvar");
        this.camposGeracao.clear();
        if (novosCampos != null) {
            this.camposGeracao.putAll(novosCampos);
        }
    }

    /** O texto de uma versao anterior volta a ser o atual, como uma versao nova. */
    public void restaurar(VersaoDocumento versao) {
        if (versao == null || id == null || !id.equals(versao.documentoId())) {
            throw new IllegalArgumentException("a versão informada não é desta peça");
        }
        if (versao.numero() >= versaoAtual) {
            throw new IllegalArgumentException("só se restaura versão anterior à atual (v" + versaoAtual + ")");
        }
        substituirTexto(versao.conteudo(), "a versão " + versao.numero() + " tem o mesmo texto da versão atual");
    }

    private void substituirTexto(String novoConteudo, String mensagemSemMudanca) {
        if (novoConteudo == null || novoConteudo.isBlank()) {
            throw new IllegalArgumentException("documento sem conteúdo");
        }
        String normalizado = novoConteudo.replace(QUEBRA_WINDOWS, QUEBRA);
        // O estado e consultado antes de comparar: em revisao, nem a tentativa passa.
        this.status = status.editar(this);
        if (normalizado.equals(conteudo)) {
            throw new IllegalArgumentException(mensagemSemMudanca);
        }
        this.conteudo = normalizado;
        this.versaoAtual++;
    }

    /**
     * Exclusao definitiva: so rascunho que nunca foi para revisao (State), e so
     * por quem gerou a peca ou pelo chefe.
     */
    public void exigirExclusaoPermitida(String oabSolicitante, boolean chefe) {
        if (!status.admiteExclusao() || !historico.isEmpty()) {
            throw new IllegalStateException("só rascunho que nunca foi para revisão pode ser excluído;"
                    + " esta peça está " + status.nome());
        }
        boolean autor = oabSolicitante != null && oabSolicitante.equalsIgnoreCase(geradoPorOab);
        if (!autor && !chefe) {
            throw new IllegalStateException("só quem gerou a peça (OAB " + geradoPorOab
                    + ") ou o chefe do escritório pode excluí-la");
        }
    }

    private void transicionar(StatusDocumento novoStatus, String responsavelOab, String comentario) {
        String deEstado = status.nome();
        this.status = novoStatus;
        historico.add(new RegistroAprovacao(deEstado, status.nome(), responsavelOab, comentario, LocalDateTime.now()));
    }

    /**
     * Desfaz a ultima decisao (aprovacao ou rejeicao), voltando a EM_REVISAO. Le a
     * decisao do proprio historico, entao funciona sobre o agregado recem-carregado
     * do banco - nao depende de nada guardado em memoria entre requisicoes.
     */
    public void desfazerUltimaDecisao() {
        String atual = status.nome();
        if (!"APROVADO".equals(atual) && !"REJEITADO".equals(atual)) {
            throw new IllegalStateException("não há decisão para desfazer em um documento " + atual);
        }
        RegistroAprovacao ultima = historico.isEmpty() ? null : historico.get(historico.size() - 1);
        String oab = ultima == null ? null : ultima.getResponsavelOab();
        transicionar(new EmRevisao(), oab, "desfeito: decisão revertida");
    }

    public void restaurarStatus(StatusDocumento statusAnterior, String responsavelOab, String comentario) {
        transicionar(statusAnterior, responsavelOab, comentario);
    }

    public void restaurarStatusPersistido(StatusDocumento status) {
        this.status = status;
    }

    public void restaurarHistoricoPersistido(List<RegistroAprovacao> historicoPersistido) {
        this.historico.clear();
        this.historico.addAll(historicoPersistido);
    }

    public StatusDocumento getStatus() {
        return status;
    }

    public List<RegistroAprovacao> getHistorico() {
        return List.copyOf(historico);
    }

    public Long getId() { return id; }
    public NumeroCnj getNumeroProcesso() { return numeroProcesso; }
    public TipoDocumento getTipo() { return tipo; }
    public String getConteudo() { return conteudo; }
    public LocalDate getGeradoEm() { return geradoEm; }
    public String getGeradoPorOab() { return geradoPorOab; }
    public boolean isSegredoJustica() { return segredoJustica; }
    public Set<String> getOabsHabilitadas() { return Set.copyOf(oabsHabilitadas); }
    public String getModeloCodigo() { return modeloCodigo; }
    public Map<String, String> getCamposGeracao() { return Collections.unmodifiableMap(camposGeracao); }
    public int getVersaoAtual() { return versaoAtual; }

    public void habilitarOab(String oab) {
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("OAB é obrigatória");
        }
        oabsHabilitadas.add(oab);
    }

    public void revogarOab(String oab) {
        if (segredoJustica && oabsHabilitadas.size() == 1 && oabsHabilitadas.contains(oab)) {
            throw new IllegalArgumentException(
                    "documento em segredo de justiça exige ao menos uma OAB habilitada");
        }
        oabsHabilitadas.remove(oab);
    }
}