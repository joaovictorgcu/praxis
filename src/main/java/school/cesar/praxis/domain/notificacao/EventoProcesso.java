package school.cesar.praxis.domain.notificacao;

/** Evento de dominio observado pelos interessados (Observer). */
public sealed interface EventoProcesso {

    String numeroProcesso();

    record AndamentoRegistrado(String numeroProcesso, String descricao) implements EventoProcesso {
    }

    record PrazoEmRisco(String numeroProcesso, String descricaoPrazo, int diasRestantes)
            implements EventoProcesso {
    }
}
