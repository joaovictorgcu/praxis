package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.FeriadosUseCases;
import school.cesar.praxis.domain.feriado.Abrangencia;

import java.time.LocalDate;
import java.util.List;

/** Camada de apresentacao (REST) da funcionalidade Cadastro de feriados. */
@RestController
@RequestMapping("/api/feriados")
public class FeriadoRestController {

    private final FeriadosUseCases.CadastrarFeriado cadastrar;
    private final FeriadosUseCases.ListarFeriados listar;
    private final FeriadosUseCases.RemoverFeriado remover;
    private final FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil;

    public FeriadoRestController(FeriadosUseCases.CadastrarFeriado cadastrar,
                                 FeriadosUseCases.ListarFeriados listar,
                                 FeriadosUseCases.RemoverFeriado remover,
                                 FeriadosUseCases.ConsultarDiaUtil consultarDiaUtil) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.remover = remover;
        this.consultarDiaUtil = consultarDiaUtil;
    }

    public record NovoFeriado(String descricao,
                              LocalDate data,
                              boolean repeteTodoAno,
                              Abrangencia.Nivel nivel,
                              String abrangencia) {
    }

    @PostMapping
    public FeriadosUseCases.ItemFeriado cadastrar(@RequestBody NovoFeriado corpo) {
        return cadastrar.executar(new FeriadosUseCases.CadastrarFeriado.Comando(
                corpo.descricao(),
                corpo.data(),
                corpo.repeteTodoAno(),
                corpo.nivel(),
                corpo.abrangencia()));
    }

    @GetMapping
    public List<FeriadosUseCases.ItemFeriado> listar() {
        return listar.executar();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        remover.executar(id);
        return ResponseEntity.noContent().build();
    }

    /** Efeito do cadastro no motor de prazos: este dia corre prazo? */
    @GetMapping("/dia-util")
    public FeriadosUseCases.ConsultarDiaUtil.Resposta diaUtil(@RequestParam LocalDate data) {
        return consultarDiaUtil.executar(data);
    }
}
