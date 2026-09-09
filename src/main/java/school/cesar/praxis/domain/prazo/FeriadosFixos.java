package school.cesar.praxis.domain.prazo;

import java.time.LocalDate;
import java.util.Set;

/**
 * Folha do Composite com um conjunto fechado de datas. Serve ao teste de
 * unidade, que precisa de calendario previsivel e sem banco.
 */
public class FeriadosFixos implements RegraDiaNaoUtil {

    private final Set<LocalDate> datas;

    public FeriadosFixos(Set<LocalDate> datas) {
        this.datas = Set.copyOf(datas);
    }

    @Override
    public boolean suspende(LocalDate data) {
        return datas.contains(data);
    }
}
