package school.cesar.praxis.presentation.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.cesar.praxis.application.port.in.DistribuicaoUseCases;
import school.cesar.praxis.domain.processo.Advogado;

import java.util.Map;

@RestController
@RequestMapping("/api/distribuicao")
public class DistribuicaoRestController {

    private final DistribuicaoUseCases.DistribuirProcesso distribuirProcesso;

    public DistribuicaoRestController(DistribuicaoUseCases.DistribuirProcesso distribuirProcesso) {
        this.distribuirProcesso = distribuirProcesso;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> distribuir(
            @RequestBody DistribuicaoUseCases.DistribuirProcesso.Comando comando) {
        Advogado escolhido = distribuirProcesso.executar(comando);
        return ResponseEntity.ok(Map.of(
                "nome", escolhido.nome(),
                "email", escolhido.email(),
                "oab", escolhido.oab()));
    }
}