package school.cesar.praxis.domain.documento;

import school.cesar.praxis.domain.processo.Advogado;

import java.util.Map;

/** Value Object com os insumos de uma peca: dados dos autos + campos livres. */
public record DadosDocumento(String numeroProcesso,
                             String cliente,
                             String comarca,
                             Advogado advogado,
                             Map<String, String> campos) {

    public DadosDocumento {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            throw new IllegalArgumentException("número do processo é obrigatório");
        }
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("cliente é obrigatório");
        }
        if (comarca == null || comarca.isBlank()) {
            throw new IllegalArgumentException("comarca é obrigatória");
        }
        if (advogado == null) {
            throw new IllegalArgumentException("advogado subscritor é obrigatório");
        }
        campos = campos == null ? Map.of() : Map.copyOf(campos);
    }

    public String campo(String chave, String padrao) {
        return campos.getOrDefault(chave, padrao);
    }
}
