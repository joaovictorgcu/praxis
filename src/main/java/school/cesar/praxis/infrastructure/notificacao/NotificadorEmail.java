package school.cesar.praxis.infrastructure.notificacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.notificacao.Notificador;
import school.cesar.praxis.domain.notificacao.NotificadorDecorator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Decorator concreto: acrescenta o canal e-mail. No MVP o envio e registrado em
 * log e mantido em memoria para inspecao; trocar por JavaMailSender nao muda o
 * dominio nem os demais decorators.
 */
public class NotificadorEmail extends NotificadorDecorator {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEmail.class);

    static final int LIMITE = 200;

    private final Deque<String> enviados = new ArrayDeque<>();

    public NotificadorEmail(Notificador delegado) {
        super(delegado);
    }

    @Override
    public void enviar(Notificacao notificacao) {
        super.enviar(notificacao);
        log.info("[e-mail] para={} assunto={}", notificacao.destinatario(), notificacao.assunto());
        synchronized (enviados) {
            if (enviados.size() >= LIMITE) {
                enviados.removeFirst();
            }
            enviados.addLast(notificacao.destinatario() + " | " + notificacao.assunto());
        }
    }

    public List<String> getEnviados() {
        synchronized (enviados) {
            return List.copyOf(enviados);
        }
    }
}
