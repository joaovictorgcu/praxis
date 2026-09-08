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
            throw new IllegalArgumentException("numero do processo e obrigatorio");
        }
        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException("cliente e obrigatorio");
        }
        if (comarca == null || comarca.isBlank()) {
            throw new IllegalArgumentException("comarca e obrigatoria");
        }
        if (advogado == null) {
            throw new IllegalArgumentException("advogado subscritor e obrigatorio");
        }
        campos = campos == null ? Map.of() : Map.copyOf(campos);
    }

    public String campo(String chave, String padrao) {
        return campos.getOrDefault(chave, padrao);
    }
}
