package school.cesar.praxis.application.port.out;

import java.util.List;
import java.util.Optional;
import school.cesar.praxis.domain.jurisdicao.Comarca;

public interface ComarcaRepositorio {
    Comarca salvar(Comarca cadastro);
    Optional<Comarca> porId(Long id);
    List<Comarca> listarTodas();
}
