package school.cesar.praxis.infrastructure.config;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import school.cesar.praxis.application.port.in.KanbanUseCases;
import school.cesar.praxis.application.port.out.AdvogadoRepositorio;
import school.cesar.praxis.application.port.out.KanbanRepositorio;
import school.cesar.praxis.domain.compartilhado.Relogio;
import school.cesar.praxis.domain.kanban.ItemChecklist;
import school.cesar.praxis.domain.kanban.PrioridadeTarefa;

@Component
@ConditionalOnProperty(name = "praxis.dados-exemplo", havingValue = "true", matchIfMissing = true)
public class TarefasKanbanDeExemplo {

    private final KanbanUseCases.ConsultarQuadro consultar;
    private final KanbanUseCases.CriarTarefa criar;
    private final KanbanRepositorio repositorio;
    private final AdvogadoRepositorio advogados;
    private final Relogio relogio;

    public TarefasKanbanDeExemplo(
        KanbanUseCases.ConsultarQuadro consultar,
        KanbanUseCases.CriarTarefa criar,
        KanbanRepositorio repositorio,
        AdvogadoRepositorio advogados,
        Relogio relogio
    ) {
        this.consultar = consultar;
        this.criar = criar;
        this.repositorio = repositorio;
        this.advogados = advogados;
        this.relogio = relogio;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void carregar() {
        if (!repositorio.listarTarefas().isEmpty()) {
            return;
        }
        var acesso = new KanbanUseCases.Acesso(true, "");
        var quadro = consultar.executar(acesso);
        var processos = quadro
            .processos()
            .stream()
            .filter(p -> !p.sigiloso())
            .toList();
        if (processos.isEmpty() || quadro.colunas().size() < 5) {
            return;
        }
        var profissionais = advogados.listarTodos();
        Long responsavel = profissionais.isEmpty() ? null : profissionais.get(0).getId();
        String[] titulos = {
            "Revisar documentos do cliente",
            "Organizar documentos do processo",
            "Preparar minuta da petição",
            "Solicitar comprovantes ao cliente",
            "Conferir peças para protocolo",
            "Conferência inicial concluída",
        };
        int[] colunas = { 0, 0, 1, 2, 3, 4 };
        for (int i = 0; i < titulos.length; i++) {
            criar.executar(
                new KanbanUseCases.CriarTarefa.Comando(
                    processos.get(i % processos.size()).numero(),
                    quadro.colunas().get(colunas[i]).id(),
                    titulos[i],
                    "Tarefa de exemplo para explorar o fluxo de trabalho do escritório.",
                    i == 2 ? PrioridadeTarefa.ALTA : PrioridadeTarefa.NORMAL,
                    i % 2 == 0 ? responsavel : null,
                    relogio.hoje().plusDays(i == 2 ? -1 : i + 1),
                    List.of(
                        new ItemChecklist("Conferir informações do processo", i >= 3),
                        new ItemChecklist("Registrar resultado da atividade", i == 5)
                    )
                ),
                acesso
            );
        }
    }
}
