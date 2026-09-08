package school.cesar.praxis.domain.notificacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorator concreto: registra trilha de auditoria. Em escritorio de advocacia,
 * provar que o responsavel foi avisado do prazo e requisito, nao conveniencia.
 */
public class NotificadorAuditoria extends NotificadorDecorator {

    public record Registro(LocalDateTime quando, String destinatario, String assunto) {
    }

    private final List<Registro> trilha = new ArrayList<>();

    public NotificadorAuditoria(Notificador delegado) {
        super(delegado);
    }

    @Override
    public void enviar(Notificacao notificacao) {
        super.enviar(notificacao);
        trilha.add(new Registro(LocalDateTime.now(), notificacao.destinatario(), notificacao.assunto()));
    }

    public List<Registro> getTrilha() {
        return List.copyOf(trilha);
    }
}
