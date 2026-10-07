package school.cesar.praxis.application.port.out;

import java.util.List;
import java.util.Optional;
import school.cesar.praxis.domain.equipe.Equipe;

public interface EquipeRepositorio {
    void excluir(Long id);

    Equipe salvar(Equipe equipe);

    Optional<Equipe> porId(Long id);

    List<Equipe> listarTodas();
}
