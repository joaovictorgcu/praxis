package school.cesar.praxis.domain.notificacao;

/** Componente do Decorator: contrato unico de envio. */
public interface Notificador {

    void enviar(Notificacao notificacao);
}
