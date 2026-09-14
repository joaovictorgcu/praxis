package school.cesar.praxis.infrastructure.notificacao;

import school.cesar.praxis.domain.notificacao.Notificacao;
import school.cesar.praxis.domain.notificacao.Notificador;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Componente base do Decorator: entrega no painel do escritorio. Guarda em
 * memoria as notificacoes da sessao, que a UI exibe como "avisos do dia".
 */
public class NotificadorPainel implements Notificador {

    /** O painel mostra as ultimas; a trilha completa esta no banco (NotificadorAuditoria). */
    static final int LIMITE = 200;

    private final Deque<Notificacao> entregues = new ArrayDeque<>();

    @Override
    public synchronized void enviar(Notificacao notificacao) {
        if (entregues.size() >= LIMITE) {
            entregues.removeFirst();
        }
        entregues.addLast(notificacao);
    }

    public synchronized List<Notificacao> getEntregues() {
        return List.copyOf(entregues);
    }

    public synchronized void limpar() {
        entregues.clear();
    }
}
