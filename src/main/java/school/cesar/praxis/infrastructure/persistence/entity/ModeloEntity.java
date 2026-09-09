package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.documento.TipoDocumento;

/** Mapeamento objeto-relacional do agregado ModeloDocumento. */
@Entity
@Table(name = "modelo_documento",
        uniqueConstraints = @UniqueConstraint(name = "uk_modelo_codigo", columnNames = "codigo"))
public class ModeloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipo;

    /** Nulo mantem o cabecalho padrao do gerador. */
    @Column(length = 160)
    private String titulo;

    @Lob
    @Column(nullable = false)
    private String corpo;

    @Lob
    @Column(nullable = false)
    private String pedidos;

    @Column(name = "endereca_ao_juizo", nullable = false)
    private boolean enderecaAoJuizo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public void setTipo(TipoDocumento tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public String getPedidos() {
        return pedidos;
    }

    public void setPedidos(String pedidos) {
        this.pedidos = pedidos;
    }

    public boolean isEnderecaAoJuizo() {
        return enderecaAoJuizo;
    }

    public void setEnderecaAoJuizo(boolean enderecaAoJuizo) {
        this.enderecaAoJuizo = enderecaAoJuizo;
    }
}
