package school.cesar.praxis.infrastructure.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.out.KanbanRepositorio;
import school.cesar.praxis.domain.kanban.ColunaKanban;

@Component
public class ColunasKanbanIniciais implements SmartInitializingSingleton {

    private final KanbanRepositorio repositorio;

    public ColunasKanbanIniciais(KanbanRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!repositorio.listarColunas().isEmpty()) {
            return;
        }
        repositorio.salvarColuna(new ColunaKanban(null, "A fazer", "#64748b", 0, false, 0));
        repositorio.salvarColuna(new ColunaKanban(null, "Em andamento", "#3b82f6", 1, false, 0));
        repositorio.salvarColuna(new ColunaKanban(null, "Aguardando retorno", "#d97706", 2, false, 0));
        repositorio.salvarColuna(new ColunaKanban(null, "Em revisão", "#8b5cf6", 3, false, 0));
        repositorio.salvarColuna(new ColunaKanban(null, "Concluído", "#16a34a", 4, true, 0));
    }
}
