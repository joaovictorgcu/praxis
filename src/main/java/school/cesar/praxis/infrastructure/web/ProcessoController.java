package school.cesar.praxis.infrastructure.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.ProcessoService;
import school.cesar.praxis.domain.processo.Andamento;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Camada de apresentacao: converte HTTP em casos de uso. */
@RestController
@RequestMapping("/api/processos")
public class ProcessoController {

    private final ProcessoService processos;

    public ProcessoController(ProcessoService processos) {
        this.processos = processos;
    }

    public record NovoProcesso(String numeroCnj,
                               String cliente,
                               boolean segredoJustica,
                               String responsavelNome,
                               String responsavelEmail,
                               String responsavelOab) {
    }

    public record NovoAndamento(LocalDate data, String descricao, TipoAndamento tipo) {
    }

    public record NovoPrazo(String descricao, LocalDate intimacao, int dias, boolean fatal) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody NovoProcesso corpo) {
        var processo = processos.cadastrar(corpo.numeroCnj(), corpo.cliente(), corpo.segredoJustica(),
                corpo.responsavelNome(), corpo.responsavelEmail(), corpo.responsavelOab());
        return ResponseEntity.ok(Map.of(
                "id", processo.getId(),
                "numero", processo.getNumero().valor(),
                "cliente", processo.getCliente()));
    }

    @PostMapping("/{numero}/andamentos")
    public ResponseEntity<Map<String, Object>> registrar(@PathVariable String numero,
                                                        @RequestBody NovoAndamento corpo) {
        var processo = processos.registrarAndamento(numero, corpo.data(), corpo.descricao(), corpo.tipo());
        return ResponseEntity.ok(Map.of("andamentos", processo.quantidadeAndamentos()));
    }

    @PostMapping("/{numero}/prazos")
    public ResponseEntity<Map<String, Object>> abrirPrazo(@PathVariable String numero,
                                                          @RequestBody NovoPrazo corpo) {
        var prazo = processos.abrirPrazo(numero, corpo.descricao(), corpo.intimacao(),
                corpo.dias(), corpo.fatal());
        return ResponseEntity.ok(Map.of(
                "descricao", prazo.getDescricao(),
                "vencimento", prazo.getVencimento().toString(),
                "regime", prazo.getRegimeContagem()));
    }

    @GetMapping("/{numero}/linha-do-tempo")
    public List<Map<String, String>> linhaDoTempo(@PathVariable String numero) {
        return processos.linhaDoTempo(numero).stream()
                .map(a -> Map.of(
                        "data", a.getData().toString(),
                        "tipo", a.getTipo().name(),
                        "descricao", a.getDescricao()))
                .toList();
    }

    @PostMapping("/varrer-prazos")
    public Map<String, Object> varrer(@RequestParam(required = false) String hoje) {
        LocalDate data = hoje == null ? LocalDate.now() : LocalDate.parse(hoje);
        return Map.of("alertasEmitidos", processos.varrerPrazos(data));
    }
}
