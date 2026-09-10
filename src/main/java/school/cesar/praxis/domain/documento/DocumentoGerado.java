package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.compartilhado.ConteudoRestrito;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Documentos</b>: a peca ja
 * materializada e anexada aos autos.
 *
 * <p>Carrega consigo a restricao de acesso (segredo de justica e OABs habilitadas)
 * porque quem decide se um documento pode ser lido e o proprio documento - o
 * {@link DocumentoProxy} apenas faz cumprir.
 */
public class DocumentoGerado implements ConteudoRestrito {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final TipoDocumento tipo;
    private final String conteudo;
    private final LocalDate geradoEm;
    private final String geradoPorOab;
    private final boolean segredoJustica;
    private final Set<String> oabsHabilitadas = new LinkedHashSet<>();

    private StatusDocumento status;
    private final List<RegistroAprovacao> historico = new ArrayList<>();

    public DocumentoGerado(Long id,
                           NumeroCnj numeroProcesso,
                           TipoDocumento tipo,
                           String conteudo,
                           LocalDate geradoEm,
                           String geradoPorOab,
                           boolean segredoJustica,
                           Set<String> oabsHabilitadas) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("documento exige processo");
        }
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("documento sem conteudo");
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

    private void transicionar(StatusDocumento novoStatus, String responsavelOab, String comentario) {
        String deEstado = status.nome();
        this.status = novoStatus;
        historico.add(new RegistroAprovacao(deEstado, status.nome(), responsavelOab, comentario, LocalDateTime.now()));
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

    public void habilitarOab(String oab) {
        if (oab == null || oab.isBlank()) {
            throw new IllegalArgumentException("OAB e obrigatoria");
        }
        oabsHabilitadas.add(oab);
    }

    public void revogarOab(String oab) {
        if (segredoJustica && oabsHabilitadas.size() == 1 && oabsHabilitadas.contains(oab)) {
            throw new IllegalArgumentException(
                    "documento em segredo de justica exige ao menos uma OAB habilitada");
        }
        oabsHabilitadas.remove(oab);
    }
}