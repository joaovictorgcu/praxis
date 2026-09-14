package school.cesar.praxis.domain.usuario;

/**
 * Porta do dominio para proteger senha. O {@link Usuario} nunca guarda a senha
 * em texto: guarda o que este codificador devolve e so sabe perguntar se uma
 * tentativa confere. Qual algoritmo (PBKDF2, bcrypt) e detalhe de infraestrutura.
 */
public interface CodificadorDeSenha {

    String codificar(String senhaEmTexto);

    boolean confere(String senhaEmTexto, String senhaCodificada);
}
