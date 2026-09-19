package school.cesar.praxis.presentation.rest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import school.cesar.praxis.domain.agenda.ConflitoDEAudienciaException;
import school.cesar.praxis.domain.agenda.HorarioInvalidoException;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Traduz falha de dominio em status HTTP. Sem isto, invariante violada pelo
 * cliente (tipo de arquivo recusado, CNJ mal formado, codigo de modelo
 * duplicado) chega ao chamador como 500, dando a entender que o erro foi do
 * servidor quando foi da requisicao.
 *
 * <p>Restrito aos controllers REST de proposito: as telas Thymeleaf tratam a
 * propria recusa, mostrando a mensagem na pagina.
 */
@RestControllerAdvice(basePackages = "school.cesar.praxis.presentation.rest")
public class TratadorDeErrosRest {

    @ExceptionHandler(ConflitoDEAudienciaException.class)
    public ResponseEntity<Map<String, String>> conflitoDeAudiencia(ConflitoDEAudienciaException falha) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", falha.getMessage()));
    }

    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<Map<String, String>> horarioInvalido(HorarioInvalidoException falha) {
        return ResponseEntity.badRequest().body(Map.of("erro", falha.getMessage()));
    }

    /** Invariante de dominio violada pela requisicao. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requisicaoInvalida(IllegalArgumentException falha) {
        return ResponseEntity.badRequest().body(Map.of("erro", falha.getMessage()));
    }

    /**
     * Transicao de estado invalida (aprovar rascunho, cumprir prazo ja cumprido):
     * a requisicao e bem formada, mas conflita com o estado atual do agregado.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estadoInvalido(IllegalStateException falha) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", falha.getMessage()));
    }

    /** Parametro mal formado (data fora do ISO, enum desconhecido, JSON invalido). */
    @ExceptionHandler({DateTimeParseException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> parametroInvalido(Exception falha) {
        return ResponseEntity.badRequest().body(Map.of("erro", "parametro invalido: " + falha.getMessage()));
    }

    /** Constraint do banco (numero de processo, CPF, OAB duplicados). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> duplicado(DataIntegrityViolationException falha) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "registro duplicado ou referencia invalida"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException falha) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", falha.getMessage()));
    }

    /** Segredo de justica (art. 189 do CPC) barrado pelo Proxy. */
    @ExceptionHandler(ProxyDeAcesso.AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(
            ProxyDeAcesso.AcessoNegadoException falha) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", falha.getMessage()));
    }
}
