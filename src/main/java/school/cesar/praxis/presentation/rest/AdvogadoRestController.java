package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.cesar.praxis.application.port.in.AdvogadosUseCases;

import java.util.List;

@RestController
@RequestMapping("/api/advogados")
public class AdvogadoRestController {

    private final AdvogadosUseCases.CadastrarAdvogado cadastrar;
    private final AdvogadosUseCases.ListarAdvogados listar;
    private final AdvogadosUseCases.PesquisarAdvogados pesquisar;
    private final AdvogadosUseCases.BuscarAdvogadoPorId buscarPorId;
    private final AdvogadosUseCases.AtualizarAdvogado atualizar;
    private final AdvogadosUseCases.AtivarAdvogado ativar;
    private final AdvogadosUseCases.DesativarAdvogado desativar;

    public AdvogadoRestController(AdvogadosUseCases.CadastrarAdvogado cadastrar,
                                  AdvogadosUseCases.ListarAdvogados listar,
                                  AdvogadosUseCases.PesquisarAdvogados pesquisar,
                                  AdvogadosUseCases.BuscarAdvogadoPorId buscarPorId,
                                  AdvogadosUseCases.AtualizarAdvogado atualizar,
                                  AdvogadosUseCases.AtivarAdvogado ativar,
                                  AdvogadosUseCases.DesativarAdvogado desativar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.pesquisar = pesquisar;
        this.buscarPorId = buscarPorId;
        this.atualizar = atualizar;
        this.ativar = ativar;
        this.desativar = desativar;
    }

    public record NovoAdvogado(String nome,
                               String email,
                               String oab,
                               String telefone,
                               String especialidade,
                               boolean disponivel) {
    }

    public record AtualizacaoAdvogado(String nome,
                                      String email,
                                      String telefone,
                                      String especialidade,
                                      boolean disponivel) {
    }

    @PostMapping
    public ResponseEntity<AdvogadosUseCases.ItemAdvogado> cadastrar(@RequestBody NovoAdvogado corpo) {
        AdvogadosUseCases.ItemAdvogado advogado = cadastrar.executar(new AdvogadosUseCases.CadastrarAdvogado.Comando(
                corpo.nome(), corpo.email(), corpo.oab(), corpo.telefone(), corpo.especialidade(), corpo.disponivel()));
        return ResponseEntity.ok(advogado);
    }

    @GetMapping
    public List<AdvogadosUseCases.ItemAdvogado> listar(@RequestParam(required = false) String busca) {
        if (busca == null || busca.isBlank()) {
            return listar.executar();
        }
        return pesquisar.executar(busca);
    }

    @GetMapping("/{id}")
    public AdvogadosUseCases.ItemAdvogado buscarPorId(@PathVariable Long id) {
        return buscarPorId.executar(id);
    }

    @PutMapping("/{id}")
    public AdvogadosUseCases.ItemAdvogado atualizar(@PathVariable Long id, @RequestBody AtualizacaoAdvogado corpo) {
        return atualizar.executar(new AdvogadosUseCases.AtualizarAdvogado.Comando(
                id, corpo.nome(), corpo.email(), corpo.telefone(), corpo.especialidade(), corpo.disponivel()));
    }

    @PostMapping("/{id}/ativar")
    public AdvogadosUseCases.ItemAdvogado ativar(@PathVariable Long id) {
        return ativar.executar(id);
    }

    @PostMapping("/{id}/desativar")
    public AdvogadosUseCases.ItemAdvogado desativar(@PathVariable Long id) {
        return desativar.executar(id);
    }
}
