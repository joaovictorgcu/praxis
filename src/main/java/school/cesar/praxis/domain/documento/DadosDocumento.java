package school.cesar.praxis.domain.documento;

import java.util.Map;

/** Insumos para geracao de peca processual. */
public record DadosDocumento(String numeroProcesso,
                             String cliente,
                             String comarca,
                             Map<String, String> campos) {
}
