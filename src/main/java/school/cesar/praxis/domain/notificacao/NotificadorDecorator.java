package school.cesar.praxis.domain.notificacao;

/** Decorator abstrato: acrescenta canais/efeitos sem alterar o componente base. */
public abstract class NotificadorDecorator implements Notificador {

    protected final Notificador delegado;

    protected NotificadorDecorator(Notificador delegado) {
        this.delegado = delegado;
    }

    @Override
    public void enviar(Notificacao notificacao) {
        delegado.enviar(notificacao);
    }
}
