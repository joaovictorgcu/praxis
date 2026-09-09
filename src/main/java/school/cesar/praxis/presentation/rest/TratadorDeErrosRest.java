package school.cesar.praxis.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import school.cesar.praxis.domain.compartilhado.ProxyDeAcesso;

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

    /** Invariante de dominio violada pela requisicao. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requisicaoInvalida(IllegalArgumentException falha) {
        return ResponseEntity.badRequest().body(Map.of("erro", falha.getMessage()));
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
