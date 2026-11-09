package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;
import school.cesar.praxis.domain.documento.OrigemVersao;

import java.time.LocalDateTime;

/**
 * Uma versao do texto da peca ({@code documento_versao}). Referencia a peca pelo
 * id (chave estrangeira na migracao) e nao pela associacao JPA: a peca e
 * carregada na lista inteira de pecas, e as versoes (texto completo cada uma)
 * so na ficha da peca.
 */
@Entity
@Table(name = "documento_versao",
        uniqueConstraints = @UniqueConstraint(name = "uk_documento_versao", columnNames = {"documento_id", "numero"}),
        indexes = @Index(name = "idx_documento_versao_documento", columnList = "documento_id"))
public class DocumentoVersaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documento_id", nullable = false)
    private Long documentoId;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = DocumentoEntity.TEXTO_LONGO)
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemVersao origem;

    @Column(name = "autor_oab", length = 20)
    private String autorOab;

    @Column(name = "autor_nome", length = 150)
    private String autorNome;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(length = 300)
    private String resumo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public OrigemVersao getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemVersao origem) {
        this.origem = origem;
    }

    public String getAutorOab() {
        return autorOab;
    }

    public void setAutorOab(String autorOab) {
        this.autorOab = autorOab;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }
}
