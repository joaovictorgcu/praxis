package school.cesar.praxis.application.port.in;

import school.cesar.praxis.domain.escritorio.Escritorio;

/** Portas de entrada do subdominio generico <b>Acesso</b>: login e cadastro de escritorio. */
public interface EscritoriosUseCases {

    /** Autentica por e-mail e senha; falha lanca {@code CredenciaisInvalidasException}. */
    interface Autenticar {

        record Comando(String email, String senha) {
        }

        Escritorio executar(Comando comando);
    }

    interface CadastrarEscritorio {

        record Comando(String nome, String cnpj, String email, String telefone,
                       String uf, String comarca, String senha) {
        }

        Escritorio executar(Comando comando);
    }
}
