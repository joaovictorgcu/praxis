package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Documentos</b>: a peca ja
 * materializada e anexada aos autos.
 *
 * <p>Carrega consigo a restricao de acesso (segredo de justica e OABs habilitadas)
 * porque quem decide se um documento pode ser lido e o proprio documento - o
 * {@link DocumentoProxy} apenas faz cumprir.
 */
public class DocumentoGerado {

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final TipoDocumento tipo;
    private final String conteudo;
    private final LocalDate geradoEm;
    private final String geradoPorOab;
    private final boolean segredoJustica;
    private final Set<String> oabsHabilitadas;

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
        this.oabsHabilitadas = oabsHabilitadas == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(oabsHabilitadas));
    }

    /** Art. 189 do CPC: em segredo de justica, so quem esta habilitado nos autos le. */
    public boolean podeSerLidoPor(String oabSolicitante) {
        if (!segredoJustica) {
            return true;
        }
        return oabSolicitante != null && oabsHabilitadas.contains(oabSolicitante);
    }

    public String nomeArquivo() {
        return tipo.name().toLowerCase() + "-" + numeroProcesso.valor().replace('.', '-') + ".txt";
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public LocalDate getGeradoEm() {
        return geradoEm;
    }

    public String getGeradoPorOab() {
        return geradoPorOab;
    }

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public Set<String> getOabsHabilitadas() {
        return oabsHabilitadas;
    }
}
