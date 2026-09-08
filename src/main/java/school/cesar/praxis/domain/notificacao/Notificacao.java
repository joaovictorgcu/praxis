package school.cesar.praxis.domain.notificacao;

/** Value Object: mensagem entregue ao advogado responsavel. */
public record Notificacao(String destinatario, String assunto, String corpo, String numeroProcesso) {
}
