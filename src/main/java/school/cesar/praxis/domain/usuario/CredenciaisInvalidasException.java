package school.cesar.praxis.domain.usuario;

/**
 * Falha de dominio: e-mail desconhecido ou senha errada. Uma mensagem so para
 * os dois casos, de proposito - dizer qual dos dois falhou revela quem tem conta.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("e-mail ou senha invalidos");
    }
}
