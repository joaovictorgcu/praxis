package school.cesar.praxis.domain.kanban;

public record ColunaKanban(Long id, String nome, String cor, int ordem, boolean conclusiva, long versao) {
    public ColunaKanban {
        if (nome == null || nome.isBlank() || nome.trim().length() > 60) {
            throw new IllegalArgumentException("O nome da coluna deve ter entre 1 e 60 caracteres.");
        }
        if (cor == null || !cor.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Selecione uma cor válida para a coluna.");
        }
        if (ordem < 0) {
            throw new IllegalArgumentException("A posição da coluna não pode ser negativa.");
        }
        nome = nome.trim();
        cor = cor.toLowerCase(java.util.Locale.ROOT);
    }
}
