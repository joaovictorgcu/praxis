package school.cesar.praxis.domain.kanban;

public record ItemChecklist(String texto, boolean concluido) {
    public ItemChecklist {
        if (texto == null || texto.isBlank() || texto.trim().length() > 200) {
            throw new IllegalArgumentException("Cada item da checklist deve ter entre 1 e 200 caracteres.");
        }
        texto = texto.trim();
    }
}
