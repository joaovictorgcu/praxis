package school.cesar.praxis.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.cesar.praxis.application.port.in.UsuariosUseCases;
import school.cesar.praxis.application.port.out.UsuarioRepositorio;
import school.cesar.praxis.domain.usuario.CodificadorDeSenha;
import school.cesar.praxis.domain.usuario.CredenciaisInvalidasException;
import school.cesar.praxis.domain.usuario.Usuario;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Casos de uso de acesso. O servico nao sabe como a senha e protegida: pede ao
 * agregado, que pede ao {@link CodificadorDeSenha}.
 */
@Service
public class UsuarioAppService implements
        UsuariosUseCases.Autenticar,
        UsuariosUseCases.CadastrarUsuario,
        UsuariosUseCases.ListarUsuarios,
        UsuariosUseCases.TrocarSenha,
        UsuariosUseCases.RemoverUsuario {

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificador;

    public UsuarioAppService(UsuarioRepositorio usuarios, CodificadorDeSenha codificador) {
        this.usuarios = usuarios;
        this.codificador = codificador;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario executar(UsuariosUseCases.Autenticar.Comando comando) {
        if (comando.email() == null || comando.email().isBlank()) {
            throw new CredenciaisInvalidasException();
        }
        Usuario usuario = usuarios.porEmail(Usuario.normalizarEmail(comando.email()))
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!usuario.senhaConfere(comando.senha(), codificador)) {
            throw new CredenciaisInvalidasException();
        }
        return usuario;
    }

    @Override
    @Transactional
    public Usuario executar(UsuariosUseCases.CadastrarUsuario.Comando comando) {
        Usuario novo = Usuario.novo(comando.nome(), comando.email(), comando.oab(),
                comando.papel(), comando.senha(), codificador, comando.senhaProvisoria());
        if (usuarios.porEmail(novo.getEmail()).isPresent()) {
            throw new IllegalArgumentException("já existe usuário com o e-mail " + novo.getEmail());
        }
        if (usuarios.porOab(novo.getOab()).isPresent()) {
            throw new IllegalArgumentException("já existe usuário com a OAB " + novo.getOab());
        }
        return usuarios.salvar(novo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> executar() {
        return usuarios.listar();
    }

    @Override
    @Transactional
    public Usuario executar(UsuariosUseCases.TrocarSenha.Comando comando) {
        Usuario usuario = usuarios.porId(comando.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("usuário não encontrado: " + comando.usuarioId()));
        if (!usuario.senhaConfere(comando.senhaAtual(), codificador)) {
            throw new IllegalArgumentException("senha atual não confere");
        }
        if (comando.novaSenha() != null && comando.novaSenha().equals(comando.senhaAtual())) {
            throw new IllegalArgumentException("a nova senha deve ser diferente da atual");
        }
        return usuarios.salvar(usuario.comSenha(comando.novaSenha(), codificador));
    }

    @Override
    @Transactional
    public void executar(UsuariosUseCases.RemoverUsuario.Comando comando) {
        if (comando.usuarioId().equals(comando.solicitanteId())) {
            throw new IllegalArgumentException("não é possível remover o próprio usuário");
        }
        Usuario alvo = usuarios.porId(comando.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("usuário não encontrado: " + comando.usuarioId()));
        if (alvo.isChefe()) {
            long chefes = usuarios.listar().stream().filter(Usuario::isChefe).count();
            if (chefes <= 1) {
                throw new IllegalArgumentException("o escritório precisa de ao menos um chefe");
            }
        }
        usuarios.remover(alvo.getId());
    }
}
