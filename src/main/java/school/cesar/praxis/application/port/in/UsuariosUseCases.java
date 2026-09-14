package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.usuario.Papel;
import school.cesar.praxis.domain.usuario.Usuario;

import java.util.List;

/** Portas de entrada do subdominio generico <b>Acesso</b>: login e cadastro de usuarios. */
public interface UsuariosUseCases {

    /** Autentica por e-mail e senha; falha lanca {@code CredenciaisInvalidasException}. */
    interface Autenticar {

        record Comando(String email, String senha) {
        }

        Usuario executar(Comando comando);
    }

    interface CadastrarUsuario {

        /** {@code senhaProvisoria}: exige troca no primeiro acesso (padrao para cadastro pelo chefe). */
        record Comando(String nome, String email, String oab, Papel papel, String senha, boolean senhaProvisoria) {

            public Comando(String nome, String email, String oab, Papel papel, String senha) {
                this(nome, email, oab, papel, senha, true);
            }
        }

        Usuario executar(Comando comando);
    }

    interface ListarUsuarios {

        List<Usuario> executar();
    }

    /** Exige a senha atual: sessao aberta nao basta para trocar a senha. */
    interface TrocarSenha {

        record Comando(Long usuarioId, String senhaAtual, String novaSenha) {
        }

        Usuario executar(Comando comando);
    }

    /** Chefe remove usuario; nunca a si mesmo nem o ultimo chefe. */
    interface RemoverUsuario {

        record Comando(Long usuarioId, Long solicitanteId) {
        }

        void executar(Comando comando);
    }
}
