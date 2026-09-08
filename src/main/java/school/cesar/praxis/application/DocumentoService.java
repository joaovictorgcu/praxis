package school.cesar.praxis.application;

import org.springframework.stereotype.Service;
import school.cesar.praxis.domain.documento.*;

import java.util.Map;

/** Pipeline de geracao de pecas (Template Method) exposto como caso de uso. */
@Service
public class DocumentoService {

    private final Map<String, GeradorDocumento> geradores = Map.of(
            "PETICAO_INICIAL", new PeticaoInicial(),
            "CONTESTACAO", new Contestacao(),
            "PROCURACAO", new Procuracao());

    public String gerar(String tipo, DadosDocumento dados) {
        GeradorDocumento gerador = geradores.get(tipo);
        if (gerador == null) {
            throw new IllegalArgumentException("tipo de peca desconhecido: " + tipo);
        }
        return gerador.gerar(dados);
    }
}
