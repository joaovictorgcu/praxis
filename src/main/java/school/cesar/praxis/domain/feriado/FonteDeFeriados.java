package school.cesar.praxis.domain.feriado;

import java.util.List;

/**
 * Porta de leitura declarada pelo proprio dominio, para que o
 * {@link school.cesar.praxis.domain.prazo.CalendarioForense} possa consultar o
 * cadastro sem conhecer persistencia - mesmo arranjo de {@code AcessoDocumento}.
 */
public interface FonteDeFeriados {

    List<Feriado> vigentes();
}
