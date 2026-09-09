package school.cesar.praxis.domain.notificacao;

/**
 * Observer concreto: traduz evento de dominio em notificacao para o advogado
 * responsavel e entrega pela cadeia de notificadores (Decorator).
 */
public class AdvogadoResponsavel implements ObservadorProcesso {

    private final Notificador notificador;

    public AdvogadoResponsavel(Notificador notificador) {
        this.notificador = notificador;
    }

    @Override
    public void notificar(EventoProcesso evento) {
        String assunto;
        String corpo;

        if (evento instanceof EventoProcesso.PrazoEmRisco risco) {
            assunto = "[" + risco.nivel() + "] Prazo em " + risco.diasRestantes()
                    + " dia(s) - processo " + risco.numeroProcesso();
            corpo = "O prazo \"" + risco.descricaoPrazo() + "\" do processo "
                    + risco.numeroProcesso() + " vence em " + risco.vencimento()
                    + " (" + risco.diasRestantes() + " dia(s) util(eis) restante(s)).";
        } else if (evento instanceof EventoProcesso.PrazoVencido vencido) {
            assunto = "[VENCIDO] Prazo perdido - processo " + vencido.numeroProcesso();
            corpo = "O prazo \"" + vencido.descricaoPrazo() + "\" venceu em "
                    + vencido.vencimento() + " sem cumprimento registrado.";
        } else if (evento instanceof EventoProcesso.AndamentoRegistrado andamento) {
            assunto = "Novo andamento - processo " + andamento.numeroProcesso();
            corpo = andamento.data() + ": " + andamento.descricao();
        } else if (evento instanceof EventoProcesso.DocumentoGerado documento) {
            assunto = "Documento gerado - processo " + documento.numeroProcesso();
            corpo = "Peca " + documento.tipoDocumento() + " gerada e anexada aos autos.";
        } else if (evento instanceof EventoProcesso.ArquivoAnexado anexo) {
            assunto = "Arquivo juntado - processo " + anexo.numeroProcesso();
            corpo = "O arquivo \"" + anexo.nomeArquivo() + "\" foi juntado aos autos.";
        } else {
            return;
        }

        notificador.enviar(new Notificacao(
                evento.destinatario().email(), assunto, corpo, evento.numeroProcesso()));
    }
}
