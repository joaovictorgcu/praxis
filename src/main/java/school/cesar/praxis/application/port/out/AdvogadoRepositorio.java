package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.advogado.Advogado;

import java.util.List;
import java.util.Optional;

public interface AdvogadoRepositorio {

    Advogado salvar(Advogado advogado);

    Optional<Advogado> porOab(String oab);

    Optional<Advogado> porId(Long id);

    List<Advogado> listarTodos();

    List<Advogado> buscar(String termo);

    List<Advogado> listarPorIds(List<Long> ids);
}
