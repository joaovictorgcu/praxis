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
    private final HonorariosUseCases.ConsultarDetalheContrato consultarDetalhe;
    private final HonorariosUseCases.RegistrarPagamentoParcela registrarPagamento;
    private final HonorariosUseCases.RegistrarResultadoCausa registrarResultado;

    public ContratoHonorarioRestController(HonorariosUseCases.CadastrarContrato cadastrar,
                                           HonorariosUseCases.ListarContratos listar,
                                           HonorariosUseCases.ConsultarDetalheContrato consultarDetalhe,
                                           HonorariosUseCases.RegistrarPagamentoParcela registrarPagamento,
                                           HonorariosUseCases.RegistrarResultadoCausa registrarResultado) {
        this.cadastrar = cadastrar;
        this.listar = listar;
        this.consultarDetalhe = consultarDetalhe;
        this.registrarPagamento = registrarPagamento;
        this.registrarResultado = registrarResultado;
    }

    public record NovoContrato(@NotBlank String numeroProcesso,
                               @NotBlank String modalidade,
                               LocalDate celebradoEm,
                               BigDecimal valorFixo,
                               BigDecimal valorHora,
                               Integer horasTrabalhadas,
                               BigDecimal valorCausa,
                               BigDecimal percentualExito,
                               Integer quantidadeParcelas,
                               LocalDate primeiroVencimento) {
    }

    public record NovoPagamento(BigDecimal valor, LocalDate dataPagamento) {
    }

    public record NovoResultado(BigDecimal valorObtido, BigDecimal honorariosSucumbencia) {
    }

    @PostMapping
    public HonorariosUseCases.ItemContrato cadastrar(@Valid @RequestBody NovoContrato corpo) {
        LocalDate celebradoEm = corpo.celebradoEm() == null ? LocalDate.now() : corpo.celebradoEm();
        return cadastrar.executar(new HonorariosUseCases.CadastrarContrato.Comando(
                corpo.numeroProcesso(),
                corpo.modalidade(),
                celebradoEm,
                corpo.valorFixo(),
                corpo.valorHora(),
                corpo.horasTrabalhadas() == null ? 0 : corpo.horasTrabalhadas(),
                corpo.valorCausa(),
                corpo.percentualExito(),
                corpo.quantidadeParcelas() == null ? 1 : corpo.quantidadeParcelas(),
                corpo.primeiroVencimento() == null ? celebradoEm : corpo.primeiroVencimento()));
    }

    @GetMapping
    public List<HonorariosUseCases.ItemContrato> listar(
            @RequestParam(required = false) String processo) {
        return listar.executar(processo);
    }

    @GetMapping("/{id}")
    public HonorariosUseCases.DetalheContrato consultar(@PathVariable Long id) {
        return consultarDetalhe.executar(new HonorariosUseCases.ConsultarDetalheContrato.Comando(id));
    }

    @PostMapping("/{id}/parcelas/{numero}/pagamentos")
    public HonorariosUseCases.DetalheContrato registrarPagamento(@PathVariable Long id, @PathVariable int numero,
                                                                  @RequestBody NovoPagamento corpo) {
        return registrarPagamento.executar(new HonorariosUseCases.RegistrarPagamentoParcela.Comando(
                id, numero, corpo.valor(), corpo.dataPagamento()));
    }

    @PostMapping("/{id}/resultado")
    public HonorariosUseCases.DetalheContrato registrarResultado(@PathVariable Long id,
                                                                  @RequestBody NovoResultado corpo) {
        return registrarResultado.executar(new HonorariosUseCases.RegistrarResultadoCausa.Comando(
                id, corpo.valorObtido(), corpo.honorariosSucumbencia()));
    }
}
