package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "equipe")
public class EquipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "equipe_membro", joinColumns = @JoinColumn(name = "equipe_id"))
    @Column(name = "advogado_id")
    private Set<Long> membrosIds = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Set<Long> getMembrosIds() {
        return membrosIds;
    }

    public void setMembrosIds(Set<Long> membrosIds) {
        this.membrosIds = membrosIds;
    }
}
