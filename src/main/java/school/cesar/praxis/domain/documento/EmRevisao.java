package school.cesar.praxis.domain.documento;

public class EmRevisao implements StatusDocumento {

    @Override
    public StatusDocumento enviarParaRevisao(DocumentoGerado documento) {
        return transicaoInvalida("reenviar para revisao");
    }

    @Override
    public StatusDocumento aprovar(DocumentoGerado documento, String oabAprovador, String comentario) {
        if (oabAprovador == null || oabAprovador.isBlank()) {
            throw new IllegalArgumentException("aprovacao exige OAB do responsavel");
        }
        return new Aprovado();
    }

    @Override
    public StatusDocumento rejeitar(DocumentoGerado documento, String oabAprovador, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("rejeicao exige motivo");
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