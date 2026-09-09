package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.feriado.Feriado;
import school.cesar.praxis.domain.feriado.FonteDeFeriados;

import java.util.Optional;

/**
 * Porta de saida: cadastro de feriados. Estende {@link FonteDeFeriados} para
 * poder ser a origem que o {@code CalendarioForense} consulta - mesmo arranjo
 * de {@code DocumentoRepositorio} com {@code AcessoDocumento}.
 */
public interface FeriadoRepositorio extends FonteDeFeriados {

    Feriado salvar(Feriado feriado);

    Optional<Feriado> porId(Long id);

    void remover(Long id);
}
