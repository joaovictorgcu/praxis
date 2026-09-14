package school.cesar.praxis.application.port.out;

import school.cesar.praxis.domain.usuario.Usuario;

import java.util.List;
import java.util.Optional;

/** Porta de saida: persistencia do agregado Usuario. */
public interface UsuarioRepositorio {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> porId(Long id);

    Optional<Usuario> porEmail(String email);

    Optional<Usuario> porOab(String oab);

    List<Usuario> listar();

    void remover(Long id);
}
