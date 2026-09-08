package school.cesar.praxis.domain.notificacao;

import java.util.ArrayList;
import java.util.List;

/** Componente concreto: notificacao base, exibida no painel do advogado. */
public class NotificadorPainel implements Notificador {

    private final List<Notificacao> entregues = new ArrayList<>();

    @Override
    public void enviar(Notificacao notificacao) {
        entregues.add(notificacao);
    }

    public List<Notificacao> getEntregues() {
        return List.copyOf(entregues);
    }
}
