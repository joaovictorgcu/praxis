package school.cesar.praxis.domain.notificacao;

import java.util.ArrayList;
import java.util.List;

/** Decorator concreto: alem do painel, envia e-mail. */
public class NotificadorEmail extends NotificadorDecorator {

    private final List<String> emailsEnviados = new ArrayList<>();

    public NotificadorEmail(Notificador delegado) {
        super(delegado);
    }

    @Override
    public void enviar(Notificacao notificacao) {
        super.enviar(notificacao);
        emailsEnviados.add(notificacao.destinatario() + ": " + notificacao.assunto());
    }

    public List<String> getEmailsEnviados() {
        return List.copyOf(emailsEnviados);
    }
}
