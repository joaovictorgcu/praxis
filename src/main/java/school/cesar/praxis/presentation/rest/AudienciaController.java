package school.cesar.praxis.presentation.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audiencias")
public class AudienciaController {

    private final AgendaDeAudienciasUseCase agendaUseCase;

    public AudienciaController(AgendaDeAudienciasUseCase agendaUseCase) {
        this.agendaUseCase = agendaUseCase;
    }

    @PostMapping
    public ResponseEntity<AudienciaResponse> criarAudiencia(@RequestBody CriarAudienciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaUseCase.criarAudiencia(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AudienciaResponse> consultarAudiencia(@PathVariable Long id) {
        AudienciaResponse audiencia = agendaUseCase.consultarAudiencia(id);
        if (audiencia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(audiencia);
    }

    @GetMapping("/processo/{numeroProcesso}")
    public ResponseEntity<AudienciaResponse> consultarAudienciaPorProcesso(@PathVariable String numeroProcesso) {
        AudienciaResponse audiencia = agendaUseCase.consultarAudienciaPorProcesso(numeroProcesso);
        if (audiencia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(audiencia);
    }

    @GetMapping
    public ResponseEntity<List<AudienciaResponse>> listarAudiencias() {
        return ResponseEntity.ok(agendaUseCase.listarAudiencias());
    }

    @GetMapping("/sala/{sala}")
    public ResponseEntity<List<AudienciaResponse>> listarAudienciasPorSala(@PathVariable String sala) {
        return ResponseEntity.ok(agendaUseCase.listarAudienciasPorSala(sala));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<AudienciaResponse>> listarAudienciasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data fim deve ser posterior a data início");
        }
        return ResponseEntity.ok(agendaUseCase.listarAudienciasPorPeriodo(dataInicio, dataFim));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AudienciaResponse> editarAudiencia(
            @PathVariable Long id,
            @RequestBody CriarAudienciaRequest request) {
        return ResponseEntity.ok(agendaUseCase.editarAudiencia(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAudiencia(@PathVariable Long id) {
        agendaUseCase.deletarAudiencia(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    public ResponseEntity<Void> reativarAudiencia(@PathVariable Long id) {
        agendaUseCase.reativarAudiencia(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/conflitos/detectar")
    public ResponseEntity<List<AudienciaResponse>> detectarConflitos(@RequestBody CriarAudienciaRequest request) {
        return ResponseEntity.ok(agendaUseCase.detectarConflitos(request));
    }
}
