package school.cesar.praxis.presentation.rest;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import school.cesar.praxis.application.port.in.KanbanUseCases;
import school.cesar.praxis.domain.kanban.ItemChecklist;
import school.cesar.praxis.domain.kanban.PrioridadeTarefa;
import school.cesar.praxis.domain.kanban.Tarefa;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;
import school.cesar.praxis.presentation.web.seguranca.UsuarioLogado;

@RestController
@RequestMapping("/api/kanban")
public class KanbanRestController {

    private final KanbanUseCases.ConsultarQuadro consultar;
    private final KanbanUseCases.CriarTarefa criar;
    private final KanbanUseCases.AtualizarTarefa atualizar;
    private final KanbanUseCases.MoverTarefa mover;
    private final KanbanUseCases.ExcluirTarefa excluir;
    private final KanbanUseCases.AtualizarChecklist checklist;
    private final KanbanUseCases.ConfigurarColunas configurar;

    public KanbanRestController(
        KanbanUseCases.ConsultarQuadro consultar,
        KanbanUseCases.CriarTarefa criar,
        KanbanUseCases.AtualizarTarefa atualizar,
        KanbanUseCases.MoverTarefa mover,
        KanbanUseCases.ExcluirTarefa excluir,
        KanbanUseCases.AtualizarChecklist checklist,
        KanbanUseCases.ConfigurarColunas configurar
    ) {
        this.consultar = consultar;
        this.criar = criar;
        this.atualizar = atualizar;
        this.mover = mover;
        this.excluir = excluir;
        this.checklist = checklist;
        this.configurar = configurar;
    }

    public record CadastroTarefa(
        String numeroProcesso,
        Long colunaId,
        String titulo,
        String descricao,
        PrioridadeTarefa prioridade,
        Long responsavelId,
        LocalDate vencimento,
        List<ItemChecklist> checklist,
        Long versao
    ) {}

    public record Movimento(Long colunaId, Long antesDeId, long versao) {}

    public record Checklist(List<ItemChecklist> checklist, long versao) {}

    @GetMapping
    public KanbanUseCases.Quadro consultar(UsuarioLogado usuario) {
        return consultar.executar(acesso(usuario));
    }

    @PostMapping("/tarefas")
    public Tarefa criar(@RequestBody CadastroTarefa corpo, UsuarioLogado usuario) {
        return criar.executar(
            new KanbanUseCases.CriarTarefa.Comando(
                corpo.numeroProcesso(),
                corpo.colunaId(),
                corpo.titulo(),
                corpo.descricao(),
                corpo.prioridade(),
                corpo.responsavelId(),
                corpo.vencimento(),
                corpo.checklist()
            ),
            acesso(usuario)
        );
    }

    @PutMapping("/tarefas/{id}")
    public Tarefa atualizar(@PathVariable Long id, @RequestBody CadastroTarefa corpo, UsuarioLogado usuario) {
        return atualizar.executar(
            new KanbanUseCases.AtualizarTarefa.Comando(
                id,
                corpo.numeroProcesso(),
                corpo.colunaId(),
                corpo.titulo(),
                corpo.descricao(),
                corpo.prioridade(),
                corpo.responsavelId(),
                corpo.vencimento(),
                corpo.checklist(),
                versao(corpo.versao())
            ),
            acesso(usuario)
        );
    }

    @PutMapping("/tarefas/{id}/mover")
    public Tarefa mover(@PathVariable Long id, @RequestBody Movimento corpo, UsuarioLogado usuario) {
        return mover.executar(
            new KanbanUseCases.MoverTarefa.Comando(id, corpo.colunaId(), corpo.antesDeId(), corpo.versao()),
            acesso(usuario)
        );
    }

    @PutMapping("/tarefas/{id}/checklist")
    public Tarefa checklist(@PathVariable Long id, @RequestBody Checklist corpo, UsuarioLogado usuario) {
        return checklist.executar(
            new KanbanUseCases.AtualizarChecklist.Comando(id, corpo.checklist(), versao(corpo.versao())),
            acesso(usuario)
        );
    }

    @DeleteMapping("/tarefas/{id}")
    public ResponseEntity<Void> excluir(
        @PathVariable Long id,
        @RequestParam long versao,
        UsuarioLogado usuario
    ) {
        excluir.executar(id, versao, acesso(usuario));
        return ResponseEntity.noContent().build();
    }

    @SomenteChefe
    @PutMapping("/colunas")
    public KanbanUseCases.Quadro configurar(
        @RequestBody KanbanUseCases.ConfigurarColunas.Comando corpo,
        UsuarioLogado usuario
    ) {
        return configurar.executar(corpo, acesso(usuario));
    }

    private static long versao(Long valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Informe a versão da tarefa antes de atualizar.");
        }
        return valor;
    }

    private static KanbanUseCases.Acesso acesso(UsuarioLogado usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar o Kanban.");
        }
        return new KanbanUseCases.Acesso(usuario.chefe(), usuario.oab());
    }
}
