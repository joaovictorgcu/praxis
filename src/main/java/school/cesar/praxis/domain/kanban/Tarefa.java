package school.cesar.praxis.domain.kanban;

import java.time.LocalDate;
import java.util.List;

public record Tarefa(
    Long id,
    String numeroProcesso,
    Long colunaId,
    String titulo,
    String descricao,
    PrioridadeTarefa prioridade,
    Long responsavelId,
    LocalDate vencimento,
    List<ItemChecklist> checklist,
    int ordem,
    LocalDate criadaEm,
    LocalDate atualizadaEm,
    long versao
) {
    public Tarefa {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            throw new IllegalArgumentException("Selecione o processo da tarefa.");
        }
        if (colunaId == null) {
            throw new IllegalArgumentException("Selecione uma coluna para a tarefa.");
        }
        if (titulo == null || titulo.isBlank() || titulo.trim().length() > 160) {
            throw new IllegalArgumentException("O título da tarefa deve ter entre 1 e 160 caracteres.");
        }
        if (descricao != null && descricao.length() > 3000) {
            throw new IllegalArgumentException("A descrição deve ter até 3000 caracteres.");
        }
        if (prioridade == null || ordem < 0 || criadaEm == null || atualizadaEm == null) {
            throw new IllegalArgumentException("Prioridade, posição e datas da tarefa são obrigatórias.");
        }
        checklist = checklist == null ? List.of() : List.copyOf(checklist);
        if (checklist.size() > 20) {
            throw new IllegalArgumentException("A checklist pode ter até 20 itens.");
        }
        numeroProcesso = numeroProcesso.trim();
        titulo = titulo.trim();
        descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    public Tarefa mover(Long destino, int posicao, LocalDate hoje) {
        return new Tarefa(
            id,
            numeroProcesso,
            destino,
            titulo,
            descricao,
            prioridade,
            responsavelId,
            vencimento,
            checklist,
            posicao,
            criadaEm,
            hoje,
            versao
        );
    }

    public Tarefa atualizarChecklist(List<ItemChecklist> itens, LocalDate hoje) {
        return new Tarefa(
            id,
            numeroProcesso,
            colunaId,
            titulo,
            descricao,
            prioridade,
            responsavelId,
            vencimento,
            itens,
            ordem,
            criadaEm,
            hoje,
            versao
        );
    }
}
