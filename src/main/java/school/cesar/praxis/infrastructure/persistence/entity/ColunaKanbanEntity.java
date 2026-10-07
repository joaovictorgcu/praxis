package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kanban_coluna")
public class ColunaKanbanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long versao;

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "cor", nullable = false, length = 7)
    private String cor;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "conclusiva", nullable = false)
    private boolean conclusiva;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersao() {
        return versao;
    }

    public void setVersao(long versao) {
        this.versao = versao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public boolean getConclusiva() {
        return conclusiva;
    }

    public void setConclusiva(boolean conclusiva) {
        this.conclusiva = conclusiva;
    }
}
