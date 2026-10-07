package school.cesar.praxis.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import school.cesar.praxis.domain.kanban.ColunaKanban;
import school.cesar.praxis.domain.kanban.ItemChecklist;
import school.cesar.praxis.domain.kanban.PrioridadeTarefa;
import school.cesar.praxis.domain.kanban.Tarefa;

public interface KanbanUseCases {
    record Acesso(boolean chefe, String oab) {}

    record ProcessoResumo(String numero, String cliente, String responsavel, boolean sigiloso) {}

    record Quadro(
        List<ColunaKanban> colunas,
        List<Tarefa> tarefas,
        List<ProcessoResumo> processos,
        String revisao,
        LocalDate hoje
    ) {}

    interface ConsultarQuadro {
        Quadro executar(Acesso acesso);
    }

    interface CriarTarefa {
        record Comando(
            String numeroProcesso,
            Long colunaId,
            String titulo,
            String descricao,
            PrioridadeTarefa prioridade,
            Long responsavelId,
            LocalDate vencimento,
            List<ItemChecklist> checklist
        ) {}

        Tarefa executar(Comando comando, Acesso acesso);
    }

    interface AtualizarTarefa {
        record Comando(
            Long id,
            String numeroProcesso,
            Long colunaId,
            String titulo,
            String descricao,
            PrioridadeTarefa prioridade,
            Long responsavelId,
            LocalDate vencimento,
            List<ItemChecklist> checklist,
            long versao
        ) {}

        Tarefa executar(Comando comando, Acesso acesso);
    }

    interface MoverTarefa {
        record Comando(Long id, Long colunaId, Long antesDeId, long versao) {}

        Tarefa executar(Comando comando, Acesso acesso);
    }

    interface ExcluirTarefa {
        void executar(Long id, long versao, Acesso acesso);
    }

    interface AtualizarChecklist {
        record Comando(Long id, List<ItemChecklist> checklist, long versao) {}

        Tarefa executar(Comando comando, Acesso acesso);
    }

    interface ConfigurarColunas {
        record Coluna(Long id, String nome, String cor, boolean conclusiva) {}

        record Comando(List<Coluna> colunas, Map<Long, Long> destinos, String revisao) {}

        Quadro executar(Comando comando, Acesso acesso);
    }
}
