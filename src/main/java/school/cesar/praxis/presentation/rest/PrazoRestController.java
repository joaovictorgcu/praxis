package school.cesar.praxis.presentation.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.PrazosUseCases;
import school.cesar.praxis.domain.prazo.AlertaPrazo;
import school.cesar.praxis.domain.prazo.Prazo;
import school.cesar.praxis.domain.prazo.RegimeContagem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Camada de apresentacao (REST) da funcionalidade Motor de prazos com alertas. */
@RestController
@RequestMapping("/api/prazos")
public class PrazoRestController {

    private final PrazosUseCases.AbrirPrazo abrirPrazo;
    private final PrazosUseCases.CumprirPrazo cumprirPrazo;
    private final PrazosUseCases.VarrerPrazos varrerPrazos;
    private final PrazosUseCases.ConsultarAgenda consultarAgenda;

    public PrazoRestController(PrazosUseCases.AbrirPrazo abrirPrazo,
                               PrazosUseCases.CumprirPrazo cumprirPrazo,
                               PrazosUseCases.VarrerPrazos varrerPrazos,
                               PrazosUseCases.ConsultarAgenda consultarAgenda) {
        this.abrirPrazo = abrirPrazo;
        this.cumprirPrazo = cumprirPrazo;
        this.varrerPrazos = varrerPrazos;
        this.consultarAgenda = consultarAgenda;
    }

    public record NovoPrazo(@NotBlank String numeroProcesso,
                            @NotBlank String descricao,
                            LocalDate intimacao,
                            @Positive int quantidadeDias,
                            boolean fatal,
                            RegimeContagem regime) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> abrir(@Valid @RequestBody NovoPrazo corpo) {
        Prazo prazo = abrirPrazo.executar(new PrazosUseCases.AbrirPrazo.Comando(
                corpo.numeroProcesso(),
                corpo.descricao(),
                corpo.intimacao(),
                corpo.quantidadeDias(),
                corpo.fatal(),
                corpo.regime() == null ? RegimeContagem.DIAS_UTEIS : corpo.regime()));

        return ResponseEntity.ok(Map.of(
                "id", prazo.getId(),
                "processo", prazo.getNumeroProcesso().valor(),
                "descricao", prazo.getDescricao(),
                "regime", prazo.getRegime().name(),
                "vencimento", prazo.getVencimento().toString(),
                "fatal", prazo.isFatal()));
    }

    @PostMapping("/{id}/cumprir")
    public Map<String, Object> cumprir(@PathVariable Long id) {
        Prazo prazo = cumprirPrazo.executar(id);
        return Map.of(
                "id", prazo.getId(),
                "cumprido", prazo.isCumprido(),
                "cumpridoEm", String.valueOf(prazo.getCumpridoEm()));
    }

    @GetMapping("/agenda")
    public List<PrazosUseCases.ConsultarAgenda.ItemAgenda> agenda(
            @RequestParam(required = false) String ate) {
        LocalDate limite = ate == null ? LocalDate.now().plusDays(30) : LocalDate.parse(ate);
        return consultarAgenda.executar(limite);
    }

    /** Varredura manual - o job diario chama exatamente o mesmo caso de uso. */
    @PostMapping("/varredura")
    public Map<String, Object> varrer(@RequestParam(required = false) String hoje) {
        List<AlertaPrazo> alertas = hoje == null
                ? varrerPrazos.executarHoje()
                : varrerPrazos.executar(LocalDate.parse(hoje));

        return Map.of(
                "alertasEmitidos", alertas.size(),
                "alertas", alertas.stream()
                        .map(alerta -> Map.of(
                                "processo", alerta.numeroProcesso(),
                                "prazo", alerta.descricaoPrazo(),
                                "nivel", alerta.nivel().name(),
                                "diasRestantes", alerta.diasRestantes(),
                                "vencimento", alerta.vencimento().toString(),
                                "destinatario", alerta.destinatario().email()))
                        .toList());
    }
}
