package school.cesar.praxis.domain.notificacao;

/** Mensagem de dominio entregue a um advogado responsavel. */
public record Notificacao(String destinatario, String assunto, String corpo) {
}
