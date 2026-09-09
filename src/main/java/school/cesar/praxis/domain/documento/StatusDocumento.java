package school.cesar.praxis.domain.documento;

public interface StatusDocumento {

    StatusDocumento enviarParaRevisao(DocumentoGerado documento);
    StatusDocumento aprovar(DocumentoGerado documento, String oabAprovador, String comentario);
    StatusDocumento rejeitar(DocumentoGerado documento, String oabAprovador, String motivo);
    StatusDocumento protocolar(DocumentoGerado documento);
    String nome();

    default StatusDocumento transicaoInvalida(String tentativa) {
        throw new IllegalStateException(
                "Nao e possivel " + tentativa + " um documento no estado " + nome());
    }

    static StatusDocumento porNome(String nome) {
        return switch (nome) {
            case "RASCUNHO" -> new Rascunho();
            case "EM_REVISAO" -> new EmRevisao();
            case "APROVADO" -> new Aprovado();
            case "REJEITADO" -> new Rejeitado();
            case "PROTOCOLADO" -> new Protocolado();
            default -> throw new IllegalArgumentException("status desconhecido: " + nome);
        };
    }
}