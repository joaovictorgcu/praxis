package school.cesar.praxis.domain.notificacao;

/** Observer concreto: o advogado responsavel pelo processo. */
public class AdvogadoResponsavel implements ObservadorProcesso {

    private final String nome;
    private final String email;
    private final Notificador notificador;

    public AdvogadoResponsavel(String nome, String email, Notificador notificador) {
        this.nome = nome;
        this.email = email;
        this.notificador = notificador;
    }

    @Override
    public void notificar(EventoProcesso evento) {
        String assunto;
        String corpo;
        if (evento instanceof EventoProcesso.PrazoEmRisco risco) {
            assunto = "PRAZO FATAL em " + risco.diasRestantes() + " dia(s): " + risco.numeroProcesso();
            corpo = "O prazo \"" + risco.descricaoPrazo() + "\" do processo "
                    + risco.numeroProcesso() + " vence em " + risco.diasRestantes() + " dia(s) util(eis).";
        } else if (evento instanceof EventoProcesso.AndamentoRegistrado andamento) {
            assunto = "Novo andamento: " + andamento.numeroProcesso();
            corpo = andamento.descricao();
        } else {
            return;
        }
        notificador.enviar(new Notificacao(email, assunto, corpo));
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
