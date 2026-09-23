package school.cesar.praxis.domain.documento;

public class EmRevisao implements StatusDocumento {

    @Override
    public StatusDocumento enviarParaRevisao(DocumentoGerado documento) {
        return transicaoInvalida("reenviar para revisão");
    }

    @Override
    public StatusDocumento aprovar(DocumentoGerado documento, String oabAprovador, String comentario) {
        if (oabAprovador == null || oabAprovador.isBlank()) {
            throw new IllegalArgumentException("aprovação exige OAB do responsável");
        }
        return new Aprovado();
    }

    @Override
    public StatusDocumento rejeitar(DocumentoGerado documento, String oabAprovador, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("rejeição exige motivo");
        }
        return new Rejeitado();
    }

    @Override
    public StatusDocumento protocolar(DocumentoGerado documento) {
        return transicaoInvalida("protocolar");
    }

    @Override
    public String nome() {
        return "EM_REVISAO";
    }
}