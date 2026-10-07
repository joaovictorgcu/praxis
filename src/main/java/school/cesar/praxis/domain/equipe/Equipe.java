package school.cesar.praxis.domain.equipe;

import java.util.LinkedHashSet;
import java.util.Set;

public class Equipe {

    private final Long id;
    private String nome;
    private final Set<Long> membrosIds = new LinkedHashSet<>();

    public Equipe(Long id, String nome, Set<Long> membrosIds) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 120) {
            throw new IllegalArgumentException("Informe um nome de equipe com até 120 caracteres");
        }
        this.id = id;
        this.nome = nome.trim();
        if (membrosIds != null) {
            this.membrosIds.addAll(membrosIds);
        }
    }

    public Equipe(String nome) {
        this(null, nome, Set.of());
    }

    public void renomear(String nome) {
        if (nome == null || nome.isBlank() || nome.trim().length() > 120) {
            throw new IllegalArgumentException("Informe um nome de equipe com até 120 caracteres");
        }
        this.nome = nome.trim();
    }

    public void adicionarMembro(Long advogadoId) {
        if (advogadoId == null) {
            throw new IllegalArgumentException("advogado e obrigatorio");
        }
        membrosIds.add(advogadoId);
    }

    public void removerMembro(Long advogadoId) {
        membrosIds.remove(advogadoId);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Set<Long> getMembrosIds() {
        return Set.copyOf(membrosIds);
    }
}
