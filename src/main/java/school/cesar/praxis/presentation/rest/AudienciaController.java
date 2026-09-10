package school.cesar.praxis.presentation.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.cesar.praxis.application.dto.AudienciaResponse;
import school.cesar.praxis.application.dto.CriarAudienciaRequest;
import school.cesar.praxis.application.port.in.AgendaDeAudienciasUseCase;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;
import school.cesar.praxis.domain.agenda.HorarioInvalidoException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para a Agenda de Audiências.
 * Endpoints para CRUD completo de audiências.
 */
@RestController
@RequestMapping("/api/audiencias")
@CrossOrigin(origins = "*")
public class AudienciaController {

    private final AgendaDeAudienciasUseCase agendaUseCase;

    public AudienciaController(AgendaDeAudienciasUseCase agendaUseCase) {
        this.agendaUseCase = agendaUseCase;
    }

    /**
     * POST /api/audiencias
     * Criar uma nova audiência
     */
    @PostMapping
    public ResponseEntity<?> criarAudiencia(@RequestBody CriarAudienciaRequest request) {
        try {
            AudienciaResponse response = agendaUseCase.criarAudiencia(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConflitoDEAudienciaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLITO_DE_HORARIO", e.getMessage()));
        } catch (HorarioInvalidoException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("HORARIO_INVALIDO", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("DADOS_INVALIDOS", e.getMessage()));
        }
    }

    /**
     * GET /api/audiencias/{id}
     * Consultar audiência por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarAudiencia(@PathVariable Long id) {
        AudienciaResponse audiencia = agendaUseCase.consultarAudiencia(id);
        if (audiencia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(audiencia);
    }

    /**
     * GET /api/audiencias/processo/{numeroProcesso}
     * Consultar audiência por número de processo
     */
    @GetMapping("/processo/{numeroProcesso}")
    public ResponseEntity<?> consultarAudienciaPorProcesso(@PathVariable String numeroProcesso) {
        AudienciaResponse audiencia = agendaUseCase.consultarAudienciaPorProcesso(numeroProcesso);
        if (audiencia == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(audiencia);
    }

    /**
     * GET /api/audiencias
     * Listar todas as audiências
     */
    @GetMapping
    public ResponseEntity<List<AudienciaResponse>> listarAudiencias() {
        List<AudienciaResponse> audiencias = agendaUseCase.listarAudiencias();
        return ResponseEntity.ok(audiencias);
    }

    /**
     * GET /api/audiencias/sala/{sala}
     * Listar audiências por sala
     */
    @GetMapping("/sala/{sala}")
    public ResponseEntity<List<AudienciaResponse>> listarAudienciasPorSala(@PathVariable String sala) {
        List<AudienciaResponse> audiencias = agendaUseCase.listarAudienciasPorSala(sala);
        return ResponseEntity.ok(audiencias);
    }

    /**
     * GET /api/audiencias/periodo?dataInicio=...&dataFim=...
     * Listar audiências por período
     */
    @GetMapping("/periodo")
    public ResponseEntity<?> listarAudienciasPorPeriodo(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        if (dataFim.isBefore(dataInicio)) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("PARAMETROS_INVALIDOS", "Data fim deve ser posterior a data início"));
        }
        
        List<AudienciaResponse> audiencias = agendaUseCase.listarAudienciasPorPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(audiencias);
    }

    /**
     * PUT /api/audiencias/{id}
     * Editar uma audiência existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarAudiencia(
        @PathVariable Long id,
        @RequestBody CriarAudienciaRequest request
    ) {
        try {
            AudienciaResponse response = agendaUseCase.editarAudiencia(id, request);
            return ResponseEntity.ok(response);
        } catch (ConflitoDEAudienciaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLITO_DE_HORARIO", e.getMessage()));
        } catch (HorarioInvalidoException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("HORARIO_INVALIDO", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("DADOS_INVALIDOS", e.getMessage()));
        }
    }

    /**
     * DELETE /api/audiencias/{id}
     * Deletar (desativar) uma audiência
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarAudiencia(@PathVariable Long id) {
        try {
            agendaUseCase.deletarAudiencia(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/audiencias/{id}/reativar
     * Reativar uma audiência desativada
     */
    @PostMapping("/{id}/reativar")
    public ResponseEntity<?> reativarAudiencia(@PathVariable Long id) {
        try {
            agendaUseCase.reativarAudiencia(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("ERRO", e.getMessage()));
        }
    }

    /**
     * POST /api/audiencias/conflitos/detectar
     * Detectar conflitos de horário para uma audiência
     */
    @PostMapping("/conflitos/detectar")
    public ResponseEntity<List<AudienciaResponse>> detectarConflitos(@RequestBody CriarAudienciaRequest request) {
        try {
            List<AudienciaResponse> conflitos = agendaUseCase.detectarConflitos(request);
            return ResponseEntity.ok(conflitos);
        } catch (HorarioInvalidoException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Classe auxiliar para resposta de erro
     */
    public static class ErrorResponse {
        public String codigo;
        public String mensagem;

        public ErrorResponse(String codigo, String mensagem) {
            this.codigo = codigo;
            this.mensagem = mensagem;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getMensagem() {
            return mensagem;
        }
    }
}
