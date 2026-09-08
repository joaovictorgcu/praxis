package school.cesar.praxis.infrastructure.notificacao;

import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.notificacao.Notificador;

import java.util.ArrayList;
import java.util.List;

/**
 * Componente base do Decorator: entrega no painel do escritorio. Guarda em
 * memoria as notificacoes da sessao, que a UI exibe como "avisos do dia".
 */
public class NotificadorPainel implements Notificador {

    private final List<Notificacao> entregues = new ArrayList<>();

    @Override
    public void enviar(Notificacao notificacao) {
        entregues.add(notificacao);
    }

    public List<Notificacao> getEntregues() {
        return List.copyOf(entregues);
    }

    public void limpar() {
        entregues.clear();
    }
}
