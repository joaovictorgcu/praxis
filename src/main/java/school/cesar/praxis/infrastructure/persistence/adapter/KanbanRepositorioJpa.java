package school.cesar.praxis.infrastructure.persistence.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import school.cesar.praxis.application.port.out.KanbanRepositorio;
import school.cesar.praxis.domain.kanban.ColunaKanban;
import school.cesar.praxis.domain.kanban.ItemChecklist;
import school.cesar.praxis.domain.kanban.Tarefa;
import school.cesar.praxis.infrastructure.persistence.entity.ColunaKanbanEntity;
import school.cesar.praxis.infrastructure.persistence.entity.ItemChecklistEntity;
import school.cesar.praxis.infrastructure.persistence.entity.TarefaKanbanEntity;
import school.cesar.praxis.infrastructure.persistence.repository.ColunaKanbanJpaRepository;
import school.cesar.praxis.infrastructure.persistence.repository.TarefaKanbanJpaRepository;

@Repository
public class KanbanRepositorioJpa implements KanbanRepositorio {

    private final ColunaKanbanJpaRepository colunas;
    private final TarefaKanbanJpaRepository tarefas;

    public KanbanRepositorioJpa(ColunaKanbanJpaRepository colunas, TarefaKanbanJpaRepository tarefas) {
        this.colunas = colunas;
        this.tarefas = tarefas;
    }

    @Override
    public List<ColunaKanban> listarColunas() {
        return colunas.findAllByOrderByOrdemAsc().stream().map(KanbanRepositorioJpa::coluna).toList();
    }

    @Override
    public List<ColunaKanban> bloquearColunas() {
        return colunas.bloquear().stream().map(KanbanRepositorioJpa::coluna).toList();
    }

    @Override
    public ColunaKanban salvarColuna(ColunaKanban coluna) {
        ColunaKanbanEntity entidade = new ColunaKanbanEntity();
        entidade.setId(coluna.id());
        entidade.setVersao(coluna.versao());
        entidade.setNome(coluna.nome());
        entidade.setCor(coluna.cor());
        entidade.setOrdem(coluna.ordem());
        entidade.setConclusiva(coluna.conclusiva());
        return coluna(colunas.saveAndFlush(entidade));
    }

    @Override
    public void excluirColuna(Long id) {
        colunas.deleteById(id);
        colunas.flush();
    }

    @Override
    public List<Tarefa> listarTarefas() {
        return tarefas.findAllByOrderByOrdemAscIdAsc().stream().map(KanbanRepositorioJpa::tarefa).toList();
    }

    @Override
    public Optional<Tarefa> tarefaPorId(Long id) {
        return tarefas.findById(id).map(KanbanRepositorioJpa::tarefa);
    }

    @Override
    public Tarefa salvarTarefa(Tarefa tarefa) {
        TarefaKanbanEntity entidade = new TarefaKanbanEntity();
        entidade.setId(tarefa.id());
        entidade.setVersao(tarefa.versao());
        entidade.setNumeroProcesso(tarefa.numeroProcesso());
        entidade.setColunaId(tarefa.colunaId());
        entidade.setTitulo(tarefa.titulo());
        entidade.setDescricao(tarefa.descricao());
        entidade.setPrioridade(tarefa.prioridade());
        entidade.setResponsavelId(tarefa.responsavelId());
        entidade.setVencimento(tarefa.vencimento());
        entidade.setOrdem(tarefa.ordem());
        entidade.setCriadaEm(tarefa.criadaEm());
        entidade.setAtualizadaEm(tarefa.atualizadaEm());
        entidade.setChecklist(
            new ArrayList<>(
                tarefa
                    .checklist()
                    .stream()
                    .map(item -> new ItemChecklistEntity(item.texto(), item.concluido()))
                    .toList()
            )
        );
        return tarefa(tarefas.saveAndFlush(entidade));
    }

    @Override
    public void excluirTarefa(Long id) {
        tarefas.deleteById(id);
        tarefas.flush();
    }

    private static ColunaKanban coluna(ColunaKanbanEntity entidade) {
        return new ColunaKanban(
            entidade.getId(),
            entidade.getNome(),
            entidade.getCor(),
            entidade.getOrdem(),
            entidade.getConclusiva(),
            entidade.getVersao()
        );
    }

    private static Tarefa tarefa(TarefaKanbanEntity entidade) {
        return new Tarefa(
            entidade.getId(),
            entidade.getNumeroProcesso(),
            entidade.getColunaId(),
            entidade.getTitulo(),
            entidade.getDescricao(),
            entidade.getPrioridade(),
            entidade.getResponsavelId(),
            entidade.getVencimento(),
            entidade
                .getChecklist()
                .stream()
                .map(item -> new ItemChecklist(item.getTexto(), item.isConcluido()))
                .toList(),
            entidade.getOrdem(),
            entidade.getCriadaEm(),
            entidade.getAtualizadaEm(),
            entidade.getVersao()
        );
    }
}
