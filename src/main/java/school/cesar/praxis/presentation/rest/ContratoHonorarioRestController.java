package school.cesar.praxis.presentation.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.HonorariosUseCases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/honorarios")
public class ContratoHonorarioRestController {

    private final HonorariosUseCases.CadastrarContrato cadastrar;
    private final HonorariosUseCases.ListarContratos listar;
    private final HonorariosUseCases.ConsultarContrato consultar;

    public ContratoHonorarioRestController(HonorariosUseCases.CadastrarContrato cadastrar,
                                           HonorariosUseCases.ListarContratos listar,
                                           HonorariosUseCases.ConsultarContrato consultar) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.consultar = consultar;
    }

    public record NovoContrato(@NotBlank String numeroProcesso,
                               @NotBlank String modalidade,
                               LocalDate celebradoEm,
                               BigDecimal valorFixo,
                               BigDecimal valorHora,
                               int horasTrabalhadas,
                               BigDecimal valorCausa,
                               BigDecimal percentualExito) {
    }

    @PostMapping
    public HonorariosUseCases.ItemContrato cadastrar(@Valid @RequestBody NovoContrato corpo) {
        return cadastrar.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                corpo.numeroProcesso(),
                corpo.modalidade(),
                corpo.celebradoEm() == null ? LocalDate.now() : corpo.celebradoEm(),
                corpo.valorFixo(),
                corpo.valorHora(),
                corpo.horasTrabalhadas(),
                corpo.valorCausa(),
                corpo.percentualExito()));
    }

    @GetMapping
    public List<HonorariosUseCases.ItemContrato> listar(
            @RequestParam(required = false) String processo) {
        return listar.executar(processo);
    }

    @GetMapping("/{id}")
    public HonorariosUseCases.ItemContrato consultar(@PathVariable Long id) {
        return consultar.executar(id);
    }
}
