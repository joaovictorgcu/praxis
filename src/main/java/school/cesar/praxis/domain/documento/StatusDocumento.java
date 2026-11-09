package school.cesar.praxis.domain.documento;

public interface StatusDocumento {

    StatusDocumento enviarParaRevisao(DocumentoGerado documento);
    StatusDocumento aprovar(DocumentoGerado documento, String oabAprovador, String comentario);
    StatusDocumento rejeitar(DocumentoGerado documento, String oabAprovador, String motivo);
    StatusDocumento protocolar(DocumentoGerado documento);

    /**
     * Mudanca no texto da peca (edicao, regeneracao ou restauracao de versao).
     * So rascunho e peca rejeitada aceitam: em revisao, o chefe esta lendo um
     * texto que nao pode mudar por baixo dele; aprovada e protocolada, o texto
     * e o que foi decidido.
     */
    StatusDocumento editar(DocumentoGerado documento);

    /** Exclusao definitiva: so o rascunho, que ninguem revisou ainda. */
    default boolean admiteExclusao() {
        return false;
    }
    String nome();

    default StatusDocumento transicaoInvalida(String tentativa) {
        throw new IllegalStateException(
                "Não é possível " + tentativa + " um documento no estado " + nome());
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