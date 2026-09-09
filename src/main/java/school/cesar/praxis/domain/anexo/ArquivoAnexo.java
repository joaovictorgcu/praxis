package school.cesar.praxis.domain.anexo;

import school.cesar.praxis.domain.compartilhado.ConteudoRestrito;
import school.cesar.praxis.domain.processo.NumeroCnj;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Documentos</b>: arquivo recebido
 * de fora e juntado aos autos (procuracao assinada, comprovante, laudo).
 *
 * <p>Agregado proprio, e nao um {@code DocumentoGerado} com bytes: a peca
 * gerada nasce de um template e tem secoes; o anexo e binario opaco, com nome
 * original, tipo e tamanho. So a <b>regra de acesso</b> e comum aos dois, e
 * vem de {@link ConteudoRestrito}.
 *
 * <p>Como o documento gerado, copia o segredo de justica e as OABs habilitadas
 * do processo no momento da juntada: anexo juntado quando os autos eram
 * publicos nao passa a ser sigiloso depois.
 */
public class ArquivoAnexo implements ConteudoRestrito {

    /** Limite por arquivo, na linha do que os autos eletronicos costumam aceitar. */
    public static final int TAMANHO_MAXIMO_BYTES = 10 * 1024 * 1024;

    private final Long id;
    private final NumeroCnj numeroProcesso;
    private final String nome;
    private final TipoArquivo tipo;
    private final byte[] conteudo;
    private final String descricao;
    private final LocalDate anexadoEm;
    private final String anexadoPorOab;
    private final boolean segredoJustica;
    private final Set<String> oabsHabilitadas;

    public ArquivoAnexo(Long id,
                        NumeroCnj numeroProcesso,
                        String nome,
                        TipoArquivo tipo,
                        byte[] conteudo,
                        String descricao,
                        LocalDate anexadoEm,
                        String anexadoPorOab,
                        boolean segredoJustica,
                        Set<String> oabsHabilitadas) {
        if (numeroProcesso == null) {
            throw new IllegalArgumentException("anexo exige processo");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("anexo exige tipo de arquivo");
        }
        if (conteudo == null || conteudo.length == 0) {
            throw new IllegalArgumentException("anexo sem conteudo");
        }
        if (conteudo.length > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("anexo excede o limite de "
                    + (TAMANHO_MAXIMO_BYTES / (1024 * 1024)) + " MB");
        }
        this.id = id;
        this.numeroProcesso = numeroProcesso;
        this.nome = nomeSeguro(nome, tipo);
        this.tipo = tipo;
        this.conteudo = conteudo.clone();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.anexadoEm = anexadoEm;
        this.anexadoPorOab = anexadoPorOab;
        this.segredoJustica = segredoJustica;
        this.oabsHabilitadas = oabsHabilitadas == null
                ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(oabsHabilitadas));
    }

    /**
     * O nome vem do computador de quem envia, entao nao e confiavel: descarta
     * caminho de diretorio (evita "../") e garante a extensao do tipo aceito.
     */
    private static String nomeSeguro(String informado, TipoArquivo tipo) {
        if (informado == null || informado.isBlank()) {
            throw new IllegalArgumentException("anexo exige nome de arquivo");
        }
        String limpo = informado.trim().replace('\\', '/');
        limpo = limpo.substring(limpo.lastIndexOf('/') + 1);
        if (limpo.isBlank()) {
            throw new IllegalArgumentException("nome de arquivo invalido: " + informado);
        }
        if (limpo.length() > 200) {
            limpo = limpo.substring(0, 200);
        }
        return limpo.toLowerCase().endsWith("." + tipo.extensao())
                ? limpo
                : limpo + "." + tipo.extensao();
    }

    /** Art. 189 do CPC: em segredo de justica, so quem esta habilitado nos autos le. */
    @Override
    public boolean podeSerLidoPor(String oabSolicitante) {
        if (!segredoJustica) {
            return true;
        }
        return oabSolicitante != null && oabsHabilitadas.contains(oabSolicitante);
    }

    public int tamanhoBytes() {
        return conteudo.length;
    }

    public Long getId() {
        return id;
    }

    public NumeroCnj getNumeroProcesso() {
        return numeroProcesso;
    }

    public String getNome() {
        return nome;
    }

    public TipoArquivo getTipo() {
        return tipo;
    }

    public byte[] getConteudo() {
        return conteudo.clone();
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getAnexadoEm() {
        return anexadoEm;
    }

    public String getAnexadoPorOab() {
        return anexadoPorOab;
    }

    public boolean isSegredoJustica() {
        return segredoJustica;
    }

    public Set<String> getOabsHabilitadas() {
        return oabsHabilitadas;
    }
}
