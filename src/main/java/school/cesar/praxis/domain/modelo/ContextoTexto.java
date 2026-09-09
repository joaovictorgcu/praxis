package school.cesar.praxis.domain.modelo;

import school.cesar.praxis.domain.documento.DadosDocumento;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Contexto de interpretacao: os valores disponiveis para os campos do modelo.
 *
 * <p>Reune os campos livres informados na geracao e os dados dos autos. Os
 * nomes reservados sao gravados por ultimo, de proposito: {@code {{cliente}}}
 * sempre vale o cliente do processo, e nao algo que o usuario tenha digitado
 * com o mesmo nome.
 */
public record ContextoTexto(Map<String, String> valores) {

    private static final Set<String> RESERVADOS =
            Set.of("processo", "cliente", "comarca", "advogado", "oab");

    public ContextoTexto {
        valores = valores == null ? Map.of() : Map.copyOf(valores);
    }

    public static ContextoTexto de(DadosDocumento dados) {
        Map<String, String> mapa = new LinkedHashMap<>(dados.campos());
        mapa.put("processo", dados.numeroProcesso());
        mapa.put("cliente", dados.cliente());
        mapa.put("comarca", dados.comarca());
        mapa.put("advogado", dados.advogado().nome());
        mapa.put("oab", dados.advogado().oab());
        return new ContextoTexto(mapa);
    }

    public Optional<String> valor(String nome) {
        String valor = valores.get(nome);
        return valor == null || valor.isBlank() ? Optional.empty() : Optional.of(valor);
    }

    /** Campos que o modelo nao precisa pedir ao usuario: vem dos autos. */
    public static Set<String> nomesReservados() {
        return RESERVADOS;
    }
}
