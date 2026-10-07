package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.escritorio.Escritorio;

import java.util.Optional;

/** Porta de saida: persistencia do agregado Escritorio. */
public interface EscritorioRepositorio {

    Escritorio salvar(Escritorio escritorio);

    Optional<Escritorio> porId(Long id);

    Optional<Escritorio> porEmail(String email);
}
