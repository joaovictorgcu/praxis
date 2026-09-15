package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.equipe.Equipe;

import java.util.List;
import java.util.Optional;

public interface EquipeRepositorio {

    Equipe salvar(Equipe equipe);

    Optional<Equipe> porId(Long id);

    List<Equipe> listarTodas();
}
