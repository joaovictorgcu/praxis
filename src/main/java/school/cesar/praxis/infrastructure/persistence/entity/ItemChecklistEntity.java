package school.cesar.praxis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemChecklistEntity {

    @Column(nullable = false, length = 200)
    private String texto;

    @Column(nullable = false)
    private boolean concluido;

    public ItemChecklistEntity() {}

    public ItemChecklistEntity(String texto, boolean concluido) {
        this.texto = texto;
        this.concluido = concluido;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isConcluido() {
        return concluido;
    }
}
