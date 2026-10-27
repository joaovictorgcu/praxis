package school.cesar.praxis.domain.compartilhado;

/**
 * Quem esta pedindo para ler um {@link ConteudoRestrito}: a OAB (que decide o
 * acesso) e, quando a chamada vem do painel, o usuario logado (so para a
 * auditoria mostrar quem, alem de qual inscricao). {@code usuarioId} e
 * {@code usuarioNome} ficam nulos quando o caso de uso e chamado direto, sem
 * passar pela sessao web - a auditoria registra "desconhecido" nesse caso.
 */
public record Solicitante(String oab, Long usuarioId, String usuarioNome) {

    public static Solicitante deOab(String oab) {
        return new Solicitante(oab, null, null);
    }
}
