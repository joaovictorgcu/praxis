package school.cesar.praxis.presentation.rest;

import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.EquipesUseCases;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

@RestController
@RequestMapping("/api/equipes")
public class EquipeRestController {

    private final EquipesUseCases.CadastrarEquipe cadastrar;
    private final EquipesUseCases.ListarEquipes listar;
    private final EquipesUseCases.AdicionarMembro adicionarMembro;
    private final EquipesUseCases.RemoverMembro removerMembro;

    private final EquipesUseCases.AtualizarEquipe atualizar;
    private final EquipesUseCases.ExcluirEquipe excluir;

    public EquipeRestController(
        EquipesUseCases.CadastrarEquipe cadastrar,
        EquipesUseCases.ListarEquipes listar,
        EquipesUseCases.AdicionarMembro adicionarMembro,
        EquipesUseCases.RemoverMembro removerMembro,
        EquipesUseCases.AtualizarEquipe atualizar,
        EquipesUseCases.ExcluirEquipe excluir
    ) {
        this.atualizar = atualizar;
        this.excluir = excluir;
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.adicionarMembro = adicionarMembro;
        this.removerMembro = removerMembro;
    }

    public record NovaEquipe(String nome, Set<Long> membrosIds) {}

    @SomenteChefe
    @PostMapping
    public ResponseEntity<EquipesUseCases.ItemEquipe> cadastrar(@RequestBody NovaEquipe corpo) {
        return ResponseEntity.ok(
            cadastrar.executar(new EquipesUseCases.CadastrarEquipe.Comando(corpo.nome(), corpo.membrosIds()))
        );
    }

    @SomenteChefe
    @PutMapping("/{id}")
    public EquipesUseCases.ItemEquipe atualizar(@PathVariable Long id, @RequestBody NovaEquipe corpo) {
        return atualizar.executar(
            new EquipesUseCases.AtualizarEquipe.Comando(id, corpo.nome(), corpo.membrosIds())
        );
    }

    @SomenteChefe
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        excluir.executar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<EquipesUseCases.ItemEquipe> listar() {
        return listar.executar();
    }

    @SomenteChefe
    @PostMapping("/{id}/membros/{advogadoId}")
    public EquipesUseCases.ItemEquipe adicionarMembro(@PathVariable Long id, @PathVariable Long advogadoId) {
        return adicionarMembro.executar(id, advogadoId);
    }

    @SomenteChefe
    @DeleteMapping("/{id}/membros/{advogadoId}")
    public EquipesUseCases.ItemEquipe removerMembro(@PathVariable Long id, @PathVariable Long advogadoId) {
        return removerMembro.executar(id, advogadoId);
    }
}
