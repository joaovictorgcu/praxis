package school.cesar.praxis.application.port.out;

import java.util.List;
import java.util.Optional;
import school.cesar.praxis.domain.kanban.ColunaKanban;
import school.cesar.praxis.domain.kanban.Tarefa;

public interface KanbanRepositorio {
    List<ColunaKanban> listarColunas();
    List<ColunaKanban> bloquearColunas();
    ColunaKanban salvarColuna(ColunaKanban coluna);
    void excluirColuna(Long id);
    List<Tarefa> listarTarefas();
    Optional<Tarefa> tarefaPorId(Long id);
    Tarefa salvarTarefa(Tarefa tarefa);
    void excluirTarefa(Long id);
}
