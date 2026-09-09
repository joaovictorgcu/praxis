package school.cesar.praxis.domain.modelo;

import school.cesar.praxis.domain.documento.TipoDocumento;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Raiz de agregado do subdominio de apoio <b>Documentos</b>: um modelo de peca
 * cadastrado pelo escritorio.
 *
 * <p>O modelo guarda apenas o que varia de peca para peca - corpo, pedidos e
 * como a peca se enderaca. A ordem das secoes continua sendo do
 * {@code GeradorDocumento}, porque e regra do dominio e nao escolha do usuario.
 *
 * <p>Nao ha versionamento: a peca gerada persiste o proprio conteudo, entao
 * editar o modelo depois nao altera documento nenhum ja produzido.
 */
public class ModeloDocumento {

    private final Long id;
    private final CodigoModelo codigo;
    private final String nome;
    private final TipoDocumento tipo;
    /** Titulo proprio da peca; nulo mantem o cabecalho padrao do gerador. */
    private final String titulo;
    private final TextoModelo corpo;
    private final TextoModelo pedidos;
    private final boolean enderecaAoJuizo;

    public ModeloDocumento(Long id,
                           CodigoModelo codigo,
                           String nome,
                           TipoDocumento tipo,
                           String titulo,
                           TextoModelo corpo,
                           TextoModelo pedidos,
                           boolean enderecaAoJuizo) {
        if (codigo == null) {
            throw new IllegalArgumentException("codigo do modelo e obrigatorio");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome do modelo e obrigatorio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("tipo da peca produzida e obrigatorio");
        }
        if (corpo == null) {
            throw new IllegalArgumentException("corpo do modelo e obrigatorio");
        }
        if (pedidos == null) {
            throw new IllegalArgumentException("pedidos do modelo sao obrigatorios");
        }
        this.id = id;
        this.codigo = codigo;
        this.nome = nome.trim();
        this.tipo = tipo;
        this.titulo = titulo == null || titulo.isBlank() ? null : titulo.trim();
        this.corpo = corpo;
        this.pedidos = pedidos;
        this.enderecaAoJuizo = enderecaAoJuizo;
    }

    public ModeloDocumento(CodigoModelo codigo,
                           String nome,
                           TipoDocumento tipo,
                           String titulo,
                           TextoModelo corpo,
                           TextoModelo pedidos,
                           boolean enderecaAoJuizo) {
        this(null, codigo, nome, tipo, titulo, corpo, pedidos, enderecaAoJuizo);
    }

    /**
     * Campos que o usuario precisa informar na geracao. Exclui os nomes
     * reservados, que a aplicacao preenche a partir dos autos.
     */
    public Set<String> camposEsperados() {
        Set<String> campos = new LinkedHashSet<>(corpo.placeholders());
        campos.addAll(pedidos.placeholders());
        campos.removeAll(ContextoTexto.nomesReservados());
        return campos;
    }

    public Long getId() {
        return id;
    }

    public CodigoModelo getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public TextoModelo getCorpo() {
        return corpo;
    }

    public TextoModelo getPedidos() {
        return pedidos;
    }

    public boolean isEnderecaAoJuizo() {
        return enderecaAoJuizo;
    }
}
