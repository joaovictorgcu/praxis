package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.port.in.ProcessosUseCases;
import school.cesar.praxis.domain.processo.Processo;
import school.cesar.praxis.domain.processo.TipoAndamento;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Camada de apresentacao (REST) do subdominio nuclear Gestao de Processos. */
@RestController
@RequestMapping("/api/processos")
public class ProcessoRestController {

    private final ProcessosUseCases.CadastrarProcesso cadastrar;
    private final ProcessosUseCases.RegistrarAndamento registrar;
    private final ProcessosUseCases.ConsultarLinhaDoTempo linhaDoTempo;

    public ProcessoRestController(ProcessosUseCases.CadastrarProcesso cadastrar,
                                  ProcessosUseCases.RegistrarAndamento registrar,
                                  ProcessosUseCases.ConsultarLinhaDoTempo linhaDoTempo) {
        this.cadastrar = cadastrar;
        this.registrar = registrar;
        this.linhaDoTempo = linhaDoTempo;
    }

    public record NovoProcesso(String numeroCnj,
                               String cliente,
                               String comarca,
                               boolean segredoJustica,
                               String responsavelNome,
                               String responsavelEmail,
                               String responsavelOab) {
    }

    public record NovoAndamento(LocalDate data, String descricao, TipoAndamento tipo) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody NovoProcesso corpo) {
        Processo processo = cadastrar.executar(new ProcessosUseCases.CadastrarProcesso.Comando(
                corpo.numeroCnj(), corpo.cliente(), corpo.comarca(), corpo.segredoJustica(),
                corpo.responsavelNome(), corpo.responsavelEmail(), corpo.responsavelOab()));

        return ResponseEntity.ok(Map.of(
                "id", processo.getId(),
                "numero", processo.getNumero().valor(),
                "cliente", processo.getCliente(),
                "comarca", processo.getComarca(),
                "segredoJustica", processo.isSegredoJustica()));
    }

    @PostMapping("/{numero}/andamentos")
    public Map<String, Object> registrarAndamento(@PathVariable String numero,
                                                  @RequestBody NovoAndamento corpo) {
        Processo processo = registrar.executar(new ProcessosUseCases.RegistrarAndamento.Comando(
                numero, corpo.data(), corpo.descricao(), corpo.tipo()));
        return Map.of("processo", numero, "andamentos", processo.quantidadeAndamentos());
    }

    @GetMapping("/{numero}/linha-do-tempo")
    public List<Map<String, String>> linhaDoTempo(@PathVariable String numero) {
        return linhaDoTempo.executar(numero).stream()
                .map(andamento -> Map.of(
                        "data", andamento.getData().toString(),
                        "tipo", andamento.getTipo().name(),
                        "descricao", andamento.getDescricao()))
                .toList();
    }
}
