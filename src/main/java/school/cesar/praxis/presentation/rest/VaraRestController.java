package school.cesar.praxis.presentation.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.VarasUseCases;
import school.cesar.praxis.presentation.web.seguranca.SomenteChefe;

@RestController
@RequestMapping("/api/varas")
public class VaraRestController {

    private final VarasUseCases.Cadastrar cadastrar;
    private final VarasUseCases.Atualizar atualizar;
    private final VarasUseCases.Listar listar;
    private final VarasUseCases.BuscarPorId buscar;

    public VaraRestController(
        VarasUseCases.Cadastrar cadastrar,
        VarasUseCases.Atualizar atualizar,
        VarasUseCases.Listar listar,
        VarasUseCases.BuscarPorId buscar
    ) {
        this.cadastrar = cadastrar;
        this.atualizar = atualizar;
        this.listar = listar;
        this.buscar = buscar;
    }

    public record Cadastro(
        Long comarcaId,
        String nome,
        String competencia,
        String endereco,
        String telefone,
        String email,
        String horarioAtendimento,
        String observacoes
    ) {}

    @GetMapping
    public List<VarasUseCases.ItemVara> listar() {
        return listar.executar();
    }

    @GetMapping("/{id}")
    public VarasUseCases.ItemVara buscar(@PathVariable Long id) {
        return buscar.executar(id);
    }

    @SomenteChefe
    @PostMapping
    public ResponseEntity<VarasUseCases.ItemVara> cadastrar(@RequestBody Cadastro corpo) {
        return ResponseEntity.ok(
            cadastrar.executar(
                new VarasUseCases.Cadastrar.Comando(
                    corpo.comarcaId(),
                    corpo.nome(),
                    corpo.competencia(),
                    corpo.endereco(),
                    corpo.telefone(),
                    corpo.email(),
                    corpo.horarioAtendimento(),
                    corpo.observacoes()
                )
            )
        );
    }

    @SomenteChefe
    @PutMapping("/{id}")
    public VarasUseCases.ItemVara atualizar(@PathVariable Long id, @RequestBody Cadastro corpo) {
        return atualizar.executar(
            new VarasUseCases.Atualizar.Comando(
                id,
                corpo.comarcaId(),
                corpo.nome(),
                corpo.competencia(),
                corpo.endereco(),
                corpo.telefone(),
                corpo.email(),
                corpo.horarioAtendimento(),
                corpo.observacoes()
            )
        );
    }
}
